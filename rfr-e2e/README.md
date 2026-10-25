# UI-тесты

Для запуска нужны JDK 25, MySQL и работающие `rfr-auth` (9001), `rfr-api` (8081),
`rfr-gql-client` (3001). Команда из корня проекта:

```powershell
./gradlew.bat :rfr-e2e:test --tests io.student.rangiffler.test.RegistrationTest
```

Тест запускает Chromium без окна (headless) в новом изолированном контексте. Playwright
при первом запуске автоматически скачивает необходимые браузеры. Для другого адреса
клиента задайте переменную окружения `RFR_BASE_URL`; адрес должен соответствовать
настройкам OAuth приложения.

Адреса собраны в `config/TestConfig`. Приоритет: system property `rfr.<key>` →
ENV `RFR_<KEY>` (точки заменяются на `_`) → константа в коде.
Пустые значения пропускаются.

| Конфигурация | System property | ENV | Значение по умолчанию |
| --- | --- | --- | --- |
| `FRONT_URL` | `rfr.base.url` | `RFR_BASE_URL` | `http://localhost:3001` |
| `AUTH_URL` | `rfr.auth.url` | `RFR_AUTH_URL` | `http://localhost:9001` |
| `API_URL` | `rfr.api.url` | `RFR_API_URL` | `http://localhost:8081` |

Gradle передаёт свойства с префиксом `rfr.` в JVM тестов. Например:

```powershell
./gradlew.bat '-Drfr.base.url=http://localhost:3001' :rfr-e2e:test --tests io.student.rangiffler.test.RegistrationTest
```

UI-тест открывает клиент, а переходы
на сервер авторизации выполняет само приложение.
Генерация логина и пароля находится в `utils/RandomDataUtils`.

Каждый запуск создаёт уникального пользователя через UI. Пользователь остаётся
в локальной БД после теста. Проверяются сообщение об успешной регистрации,
вход, открытие карты и логин пользователя в защищённом профиле.

HTML-отчёт Gradle: `build/reports/tests/test/index.html` внутри этого модуля.

## Пользователь из БД auth

`@User` на параметре автоматически подключает `UserExtension`:

```java
@Test
void shouldLogin(@User TestUser user) {
  new LandingPage(page).open().login()
      .fillLoginForm(user.username(), user.password())
      .signIn().shouldBeAuthenticated();
}
```

Импорты: `io.student.rangiffler.annotation.User` и
`io.student.rangiffler.model.TestUser`. Каждый параметр получает отдельного
пользователя с UUID, логином и исходным паролем. Spring JDBC записывает пользователя
и права `read`/`write` в одной транзакции, пароль кодируется тем же
`DelegatingPasswordEncoder`, что и в auth. Spring Boot context не запускается.
`new UserFixture()` подготавливает модель без записи в БД. Метод `create()`
создаёт пользователя в auth, `user()` возвращает
его тестовую модель, а `close()` удаляет права и пользователя через репозиторий.
Extension сначала сохраняет fixture в JUnit Store и только потом вызывает `create()`.
Поэтому Store вызовет очистку и при ошибке создания. `close()` безопасен до создания,
после неудачной попытки и при повторном вызове; удаление ограничено UUID fixture.
JUnit Store вызывает `close()` после завершения теста,
включая случай падения. Профиль в БД API, если он был создан при входе, остаётся.
Для параметров lifecycle-методов ресурс живёт в соответствующем JUnit context.

Fixture также можно использовать напрямую:

```java
try (UserFixture fixture = new UserFixture()) {
  fixture.create();
  TestUser user = fixture.user();
  // Сценарий с подготовленным пользователем.
}
```

Настройки БД используют приоритет system property → ENV → fallback:

| System property | ENV | Fallback |
| --- | --- | --- |
| `rfr.auth.db.url` | `RFR_AUTH_DB_URL` | `jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC` |
| `rfr.auth.db.username` | `RFR_AUTH_DB_USERNAME` | `root` |
| `rfr.auth.db.password` | `RFR_AUTH_DB_PASSWORD` | локальный пароль из конфигурации auth |

По умолчанию используются те же локальные реквизиты, что и в конфигурации auth.
Для другой БД переопределите их через system properties или ENV.
Схема должна быть создана миграциями auth.
`LoginTest` проверяет реальный вход пользователя из БД.
`RegistrationTest` по-прежнему регистрирует пользователя через UI.

## Работа с БД

`data/db/Databases` содержит `AUTH` и `API`: у каждой базы собственные
`DataSource` и transaction manager. API использует настройки `rfr.api.db.url`,
`rfr.api.db.username`, `rfr.api.db.password` (ENV: `RFR_API_DB_URL`,
`RFR_API_DB_USERNAME`, `RFR_API_DB_PASSWORD`); локальные fallback соответствуют
конфигурации сервера API.

`JDBC.jdbcClient(database)` возвращает Spring `JdbcClient`.
`JDBC.inTransaction(database, action)` выполняет `Runnable` или `Supplier<T>`
в транзакции выбранной БД с откатом при исключении. Внутри callback используйте
клиент этой же базы. Это транзакция одной БД, без распределённой транзакции.
`JDBC.loadSql(resourcePath)` читает SQL из classpath в UTF-8.
Все SQL E2E-модуля находятся в `src/test/resources/sql/`, значения передаются
именованными параметрами.
