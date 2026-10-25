# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

```bash
./gradlew :rfr-e2e:installPlaywright                      # один раз: браузер Chromium
./gradlew e2e                                             # все UI-тесты (тег ui)
./gradlew :rfr-e2e:e2e --tests '*MainPageTest'            # один тест
./gradlew :rfr-e2e:test                                   # всё без тега ui, стенд не нужен; с --tests тег ui не отсекается (запуск из IDE)
./gradlew :rfr-e2e:allureReport                           # rfr-e2e/build/reports/allure-report/allureReport/index.html
```

Стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README).

Тесты, которым нужен браузер и стенд, помечены `@Tag("ui")` и запускаются задачей `e2e`. Задача `test` исключает тег `ui` и обходится без стенда. Обе задачи запускают тесты при каждом вызове, `--rerun-tasks` не нужен. Перед тестами `e2e` вызывает `waitForStand` (`scripts/wait-for-stand.sh`): он ждёт MySQL, `rfr-auth`, `rfr-api` и фронт не дольше `WAIT_TIMEOUT` секунд (по умолчанию 180).

## Конфигурация

`TestConfig` читает `-Drfr.<ключ>` → `RFR_<КЛЮЧ>` → значение по умолчанию. Gradle передаёт все `-Drfr.*` в тестовую JVM.

| Ключ | По умолчанию |
|---|---|
| `rfr.frontUrl` | `http://localhost:3001` |
| `rfr.authUrl` | `http://localhost:9001` |
| `rfr.apiUrl` | `http://localhost:8081` |
| `rfr.dbUrl` | `jdbc:mysql://localhost:3306` (схема добавляется кодом) |
| `rfr.dbUser` / `rfr.dbPassword` | `root` / `secret` |
| `rfr.browser` / `rfr.headless` | `chromium` / `true` |

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `jupiter` | всё про JUnit: `annotation` (`@User`, `@ProbeTarget`), `extension` (`UserExtension`; `StandSchemaExtension` — перед задачей `e2e` сверяет миграции, регистрируется через ServiceLoader), `condition` (`ProbeTargetCondition` — мишени probe-тестов выполняются только внутри EngineTestKit), `probe` |
| `data` | тестовые данные и их владельцы: `TestUser`, `UserFixture` с реализацией `SingleUserFixture` и декораторами `UserWithFriendsFixture`, `UserWithIncomeInvitationsFixture`, `UserRequest` собирает их цепочку; `db` — `Databases`, `Jdbc` и неизменяемые объекты строк (`Row`/`Rows`: `AuthAccount`, `ApiProfile`, `Friendship`, `Friendships`), которые прячут весь SQL за `create`/`delete`/`exists`; `Migrations` сверяет схему БД с миграциями бэкенда через Flyway validate |
| `utils` | статические утилиты без состояния: `RandomDataUtils` — случайные username/password; сюда же будущие генераторы (описания фото и т.п.) |
| `ui` | `page` — Page Objects, `component` — общие части страниц |
| `test/auth` | вход и регистрация через UI |
| `test/main` | главная страница |
| `test/people` | друзья и входящие приглашения |
| `test/data` | параметры `UserRequest`; `db` — объекты строк базы данных |
| `test/fixture` | создание и очистка данных фикстурами |
| `test/extension` | контракт `UserExtension` через EngineTestKit; `probe` — наблюдение за внутренними сценариями |

Правила: в `jupiter` нет SQL и локаторов; в `data` нет JUnit; в `test` нет локаторов и SQL; `ThreadLocal` для состояния теста не используется — состояние живёт в Store расширения.

`UserExtensionTest` использует настоящую MySQL и не требует браузера. Запуск из PowerShell:

```powershell
./gradlew.bat :rfr-e2e:test --tests 'io.student.rangiffler.test.extension.UserExtensionTest'
```

Внутренние сценарии помечены `@ProbeTarget` и выполняются только при явном включении из EngineTestKit. `TestReporter` фиксирует этапы `before`, `body`, `after` и результат проверки после закрытия Store (`store-closed`), без паролей. Проверяются предусловия, сочетание декораторов, изоляция параметров и параллельных методов, очистка после успеха и ошибки, отклонение некорректных параметров.

## Агент

Page Objects пишет Claude Code по skill `rfr-page-objects`; UI исследуется через Playwright MCP (`.mcp.json`).

Доказательство корректности теста - запуск gradle таски.
