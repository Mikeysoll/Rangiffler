param([switch]$Initialize)

$ErrorActionPreference = 'Stop'
$root = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$eventData = [Console]::In.ReadToEnd() | ConvertFrom-Json
$sha = [System.Security.Cryptography.SHA256]::Create()
$key = [BitConverter]::ToString($sha.ComputeHash([Text.Encoding]::UTF8.GetBytes("$root|$($eventData.session_id)"))).Replace('-', '')
$statePath = Join-Path ([IO.Path]::GetTempPath()) "rangiffler-compile-$key.json"
$mutex = New-Object Threading.Mutex($false, "Local\RangifflerCompile$key")
$locked = $false

function Send-Context([string]$message) {
    @{ hookSpecificOutput = @{ hookEventName = 'PostToolUse'; additionalContext = $message } } |
        ConvertTo-Json -Depth 4 -Compress | Write-Output
}

try {
    $locked = $mutex.WaitOne(500000)
    if (-not $locked) { throw 'Timed out waiting for another compilation hook.' }
    Push-Location $root
    try {
        # Include tracked and untracked files, but exclude ignored build artifacts.
        $paths = @(& git ls-files --cached --others --exclude-standard -- '*.java' 'rfr-gql-client/src/*' 'rfr-gql-client/*.json' 'rfr-gql-client/*.ts' 'rfr-gql-client/index.html' '*.gradle' 'gradle.properties' | Sort-Object -Unique)
        if ($LASTEXITCODE -ne 0) { throw 'Cannot enumerate project sources.' }
        $current = @{}
        foreach ($path in $paths) {
            if (Test-Path -LiteralPath $path -PathType Leaf) {
                $current[$path] = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash
            }
        }
        $previous = @{}
        if (Test-Path -LiteralPath $statePath) {
            $saved = Get-Content -LiteralPath $statePath -Raw | ConvertFrom-Json
            foreach ($property in $saved.PSObject.Properties) { $previous[$property.Name] = $property.Value }
        }
        if ($Initialize) {
            $current | ConvertTo-Json -Compress | Set-Content -LiteralPath $statePath -Encoding UTF8
            exit 0
        }
        $changed = @(@($current.Keys) + @($previous.Keys) | Sort-Object -Unique | Where-Object { $current[$_] -ne $previous[$_] })
        if ($changed.Count -eq 0) { exit 0 }

        $tasks = @()
        foreach ($module in @('rfr-auth', 'rfr-api', 'rfr-e2e')) {
            if (@($changed | Where-Object { $_ -like "$module/*.java" -or $_ -eq "$module/build.gradle" -or $_ -in @('build.gradle', 'settings.gradle', 'gradle.properties') }).Count) {
                $tasks += ":${module}:compileTestJava"
            }
        }
        $frontend = @($changed | Where-Object { $_ -like 'rfr-gql-client/*' }).Count -gt 0
        $messages = @()
        if ($tasks.Count -gt 0) {
            if (-not $env:JAVA_HOME -and (Test-Path 'C:/Users/Biam/.jdks/openjdk-25.0.1')) {
                $env:JAVA_HOME = 'C:/Users/Biam/.jdks/openjdk-25.0.1'
            }
            $output = (& cmd.exe /d /c ".\gradlew.bat $($tasks -join ' ') --console=plain 2>&1" | Out-String)
            $messages += "Java: $($tasks -join ' ')`nExit code: $LASTEXITCODE`n$output"
        }
        if ($frontend) {
            Push-Location (Join-Path $root 'rfr-gql-client')
            try {
                $output = (& cmd.exe /d /c 'npm.cmd run build 2>&1' | Out-String)
                $messages += "Frontend: npm run build`nExit code: $LASTEXITCODE`n$output"
            } finally { Pop-Location }
        }
        # Save the checked snapshot even on compilation failure: retry after the next edit.
        $current | ConvertTo-Json -Compress | Set-Content -LiteralPath $statePath -Encoding UTF8
        if ($messages.Count) {
            $report = $messages -join "`n`n"
            $logPath = Join-Path ([IO.Path]::GetTempPath()) "rangiffler-compile-$key-$([Guid]::NewGuid().ToString('N')).log"
            $report | Set-Content -LiteralPath $logPath -Encoding UTF8
            if ($report.Length -gt 20000) { $report = $report.Substring(0, 5000) + "`n...`n" + $report.Substring($report.Length - 15000) }
            Send-Context "Automatic compilation after source changes. Fix any reported errors before finishing.`nFull output: $logPath`n$report"
        }
    } finally { Pop-Location }
} catch {
    Send-Context "Compilation hook failed: $($_.Exception.Message). Run the relevant compilation manually."
} finally {
    if ($locked) { $mutex.ReleaseMutex() }
    $mutex.Dispose()
    $sha.Dispose()
}
