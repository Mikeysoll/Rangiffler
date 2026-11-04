# rfr-e2e

E2E-тесты Rangiffler: JUnit 6.1.3, Playwright Java 1.63 (`@UsePlaywright`), Allure 3, `JdbcClient` для фикстур.

## Запуск

Команды выполняются из корня репозитория с JDK 25. Для Bash замените `./gradlew.bat` на `./gradlew`.

```powershell
./gradlew.bat :rfr-e2e:installPlaywrightWithDeps          # Chromium и его системные зависимости
./gradlew.bat e2e                                       # все тесты модуля
./gradlew.bat :rfr-e2e:test                              # та же тестовая задача напрямую
./gradlew.bat :rfr-e2e:test --tests '*MainPageTest'       # один тестовый класс
./gradlew.bat :rfr-e2e:allureReport                      # сформировать отчёт Allure
```

Стенд: MySQL `:3306`, `rfr-auth :9001`, `rfr-api :8081`, фронт `:3001` (см. корневой README).

Корневая задача `e2e` вызывает `:rfr-e2e:test`; отдельной задачи `:rfr-e2e:e2e` нет. Фильтрация по тегу `ui` не настроена: полный запуск включает браузерные тесты и требует работающего стенда. Для принудительного повторного запуска при результате `UP-TO-DATE` добавьте `--rerun-tasks`.

Gradle не запускает стенд и не ждёт его готовности. В CI это отдельный шаг перед тестами: `scripts/wait-for-stand.sh` проверяет MySQL, auth, API и фронтенд. Таймаут задаётся через `WAIT_TIMEOUT`, по умолчанию — 300 секунд.

## Конфигурация

`TestConfig` читает системное свойство → переменную окружения → значение по умолчанию. Точки в имени ключа заменяются подчёркиваниями: `rfr.base.url` → `RFR_BASE_URL`. Gradle передаёт все `-Drfr.*` в тестовую JVM.

| Ключ | По умолчанию |
|---|---|
| `rfr.base.url` | `http://localhost:3001` |
| `rfr.auth.url` | `http://localhost:9001` |
| `rfr.api.url` | `http://localhost:8081` |
| `rfr.auth.db.url` | `jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC` |
| `rfr.api.db.url` | `jdbc:mysql://localhost:3306/rangiffler-api?serverTimezone=UTC` |
| `rfr.auth.db.username` / `rfr.auth.db.password` | `root` / `secret` |
| `rfr.api.db.username` / `rfr.api.db.password` | `root` / `secret` |

Параметры `rfr.browser` и `rfr.headless` в `TestConfig` не поддерживаются. Параллельное выполнение по умолчанию отключено в `src/test/resources/junit-platform.properties`; проверка конкурентных сценариев в `UserExtensionTest` включает его отдельно.

## Архитектура

Один source set `src/test`, пакет `io.student.rangiffler`:

| Пакет | Что лежит |
|---|---|
| `config` | `TestConfig` — единственная точка чтения настроек |
| `annotation` | `@User` для параметров теста и `@ProbeTarget` для внутренних сценариев EngineTestKit |
| `extension` | `UserExtension` управляет фикстурами через JUnit Store; `ProbeTargetCondition` ограничивает запуск внутренних сценариев |
| `model` | `TestUser` — идентификатор и данные для входа |
| `data` | `UserRequest` проверяет параметры `@User` и собирает цепочку фикстур |
| `data/db` | `Databases`, `JDBC`, интерфейсы `Row`, `Rows`, `Relations` и объекты `AuthAccount`, `Profile`, `Friendship`, `Friendships`; SQL загружается из `src/test/resources/sql` |
| `fixture` | `UserFixture`, `SingleUserFixture`, декораторы `UserWithFriendsFixture` и `UserWithIncomeInvitations`; `RegistrationFixture` владеет данными регистрации, `TearDown` объединяет действия очистки |
| `utils` | статические утилиты без состояния: `RandomDataUtils` — случайные username/password; сюда же будущие генераторы (описания фото и т.п.) |
| `page` | Page Objects; `page/component` — общие части страниц |
| `test/auth` | вход и регистрация через UI |
| `test/main` | главная страница |
| `test/people` | друзья и входящие приглашения |
| `test/data` | параметры `UserRequest`; `db` — объекты строк базы данных |
| `test/fixture` | создание и очистка данных фикстурами |
| `test/extension` | контракт `UserExtension` через EngineTestKit; `probe` — наблюдение за внутренними сценариями |

Локаторы находятся в Page Objects, работа с БД — в `data/db`, владение тестовыми данными — в фикстурах. `UserExtension` сохраняет фикстуру в Store до её создания, чтобы очистка выполнялась и при ошибке подготовки. Состояние параметров привязано к текущему тесту, `ThreadLocal` не используется.

`UserExtensionTest` использует настоящую MySQL и не требует браузера. Схемы обеих БД должны быть подготовлены миграциями бэкендов; модуль тестов сам миграции не запускает и не сверяет. Запуск из PowerShell:

```powershell
./gradlew.bat :rfr-e2e:test --tests 'io.student.rangiffler.test.extension.UserExtensionTest'
```

Внутренние сценарии помечены `@ProbeTarget` и выполняются только при явном включении из EngineTestKit. `TestReporter` фиксирует этапы `before`, `body`, `after` и результат проверки после закрытия Store (`store-closed`), без паролей. Проверяются предусловия, сочетание декораторов, изоляция параметров и параллельных методов, очистка после успеха и ошибки, отклонение некорректных параметров.

## Агент

Page Objects пишет Claude Code по skill `rfr-page-objects`; UI исследуется через Playwright MCP (`.mcp.json`).

Корректность теста подтверждается запуском соответствующей задачи Gradle.
