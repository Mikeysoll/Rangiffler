# Rangiffler

Rangiffler — учебный проект приложения о путешествиях: карта посещённых стран,
фотографии, профили пользователей и друзья. Серверная часть состоит из OAuth-сервера
авторизации и GraphQL API, интерфейс написан на React.

В репозитории также развивается автоматизация UI на Java: Playwright, JUnit и Allure,
Page Object и подготовка пользователей напрямую в БД.

![Интерфейс Rangiffler](rangiffler.png)

## Возможности и текущее состояние

- Регистрация и вход через OAuth 2.0 / OpenID Connect с PKCE.
- Просмотр и редактирование профиля, выбор страны.
- Поиск пользователей, друзья, входящие и исходящие приглашения.
- Карта путешествий и лента фотографий.
- UI-тесты регистрации и входа с подготовкой пользователя через JUnit extension.

Пользователи, страны и дружба работают с MySQL. Лента, операции с фотографиями,
лайки и статистика частично используют заглушки из JSON. Поэтому новый аккаунт
может видеть демонстрационные путешествия, а изменения фотографий могут исчезать
после обновления страницы.

## Стек

| Часть | Технологии |
| --- | --- |
| Серверы | Java 25, Spring Boot 4.1.1, Spring Security, Spring Data JPA |
| API | Spring for GraphQL, DGS Codegen |
| База данных | MySQL 8.4, Flyway |
| Клиент | React 19, TypeScript 6, Vite 8, Apollo Client, Material UI |
| Автотесты | JUnit Jupiter 6.1.3, Playwright Java 1.63.0, Allure 3, Spring JDBC |
| Сборка | Gradle Wrapper 9.7.1, npm |

Версии зависимостей закреплены в Gradle-файлах и `rfr-gql-client/package-lock.json`.

## Структура

```text
Rangiffler/
├── rfr-auth/          # Регистрация, OAuth 2.0 / OIDC, права пользователей
├── rfr-api/           # GraphQL API, профили, страны, дружба, заглушки фотографий
├── rfr-gql-client/    # React-приложение
├── rfr-e2e/           # UI-тесты и тестовая инфраструктура
├── .agents/skills/    # Правила разработки Page Object
├── build.gradle      # Общая конфигурация Java-модулей
└── localenv.sh       # Скрипт окружения с удалением всех Docker-контейнеров
```

Java-модули собираются общей Gradle-сборкой. Клиент собирается отдельно через npm.

## Требования

- JDK 25 и настроенный `JAVA_HOME`.
- Node.js 22.22.0 или новее и npm.
- Docker с запущенным Docker Engine либо отдельный MySQL 8.4.
- Свободные порты 3001, 8081, 9001 и 3306.

Команды ниже предназначены для PowerShell и выполняются из корня репозитория,
если не указано иное. В Bash используйте `./gradlew` вместо `./gradlew.bat`.

## Локальный запуск

### 1. MySQL

При первом запуске создайте контейнер:

```powershell
docker run --name rfr-mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=secret -v mysql-volume:/var/lib/mysql -d mysql:8.4.7
```

Если контейнер уже существует:

```powershell
docker start rfr-mysql
```

Проверить готовность можно по журналу:

```powershell
docker logs --tail 50 rfr-mysql
```

Дождитесь сообщения `ready for connections`. Сервисы используют две схемы:
`rangiffler-auth` и `rangiffler-api`. При первом старте они создаются через JDBC,
а Flyway применяет миграции. Локальные реквизиты: пользователь `root`,
пароль `secret`; они совпадают с конфигурациями серверов и тестов.

**Перед использованием `localenv.sh` прочитайте его содержимое: скрипт останавливает
и удаляет все Docker-контейнеры, включая контейнеры других проектов.**
Для запуска только БД Rangiffler достаточно команд выше.

### 2. Сервер авторизации

В отдельном терминале:

```powershell
./gradlew.bat :rfr-auth:bootRun
```

Дождитесь сообщения `Started RangifflerAuthApplication`. Сервис слушает порт 9001.

### 3. GraphQL API

Во втором терминале:

```powershell
./gradlew.bat :rfr-api:bootRun
```

Дождитесь сообщения `Started RangifflerApiApplication`. Сервис слушает порт 8081.

### 4. Клиент

В третьем терминале:

```powershell
cd rfr-gql-client
npm ci
npm run dev
```

Откройте [http://localhost:3001](http://localhost:3001), нажмите **Register**,
создайте аккаунт, затем войдите через **Login**.

Логин должен содержать 3–50 символов, пароль — 3–12 символов.
Пробелы в логине и пароле запрещены.

### Адреса

| Назначение | Адрес |
| --- | --- |
| Приложение | [http://localhost:3001](http://localhost:3001) |
| Регистрация | [http://localhost:9001/register](http://localhost:9001/register) |
| Вход | [http://localhost:9001/login](http://localhost:9001/login) |
| GraphQL | `POST http://localhost:8081/graphql` |
| GraphiQL | [http://localhost:8081/graphiql](http://localhost:8081/graphiql) |
| MySQL | `localhost:3306` |

Используйте `localhost` последовательно: адреса клиента, OAuth redirect URI и issuer
должны совпадать с настройками приложения.

## Конфигурация приложения

Серверные настройки:

- [rfr-auth/src/main/resources/application.yaml](rfr-auth/src/main/resources/application.yaml)
- [rfr-api/src/main/resources/application.yml](rfr-api/src/main/resources/application.yml)

Для серверов можно переопределять стандартные Spring-настройки через окружение:
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
Переменные должны быть установлены в терминале, из которого запускается соответствующий сервис.

Клиент использует [rfr-gql-client/.env](rfr-gql-client/.env):

```dotenv
VITE_AUTH_URL=http://localhost:9001
VITE_API_URL=http://localhost:8081
VITE_FRONT_HOST=localhost
VITE_FRONT_URL=http://localhost:3001
VITE_CLIENT_ID=client
```

При изменении адресов согласуйте настройки клиента, CORS, issuer и OAuth redirect URI
на серверах. Изменение только тестового URL не перенастраивает приложение.

## Сборка и проверки

Собрать Java-модули без запуска тестов:

```powershell
./gradlew.bat assemble
```

Полная Java-сборка с тестами:

```powershell
./gradlew.bat build
```

Для полной сборки нужны запущенные MySQL, auth, API и клиент: в неё входят UI-тесты.

Проверить и собрать клиент:

```powershell
cd rfr-gql-client
npm run lint
npm run build
```

Модели GraphQL генерируются задачей `:rfr-api:generateJava` в `build/generated/`.
Не редактируйте их вручную; изменяйте схему и настройки codegen.

## UI-тесты

Перед запуском поднимите все сервисы по инструкции выше.

```powershell
# Весь E2E-модуль
./gradlew.bat :rfr-e2e:test

# Регистрация через UI и последующий вход
./gradlew.bat :rfr-e2e:test --tests io.student.rangiffler.test.RegistrationTest

# Вход пользователя, созданного в БД auth
./gradlew.bat :rfr-e2e:test --tests io.student.rangiffler.test.LoginTest
```

Для принудительного повторного выполнения добавьте `--rerun-tasks`.

Тесты запускают Chromium в headless-режиме, каждый в отдельном браузерном контексте.
При первом запуске Playwright автоматически скачивает браузеры.
В модуле также остаётся исходный демонстрационный `SimpleTest`.

| Тест | Проверка |
| --- | --- |
| `RegistrationTest` | Уникальный пользователь регистрируется через UI, входит и видит свой логин в профиле |
| `LoginTest` | Пользователь из auth, переданный через `@User`, входит и видит свой профиль |

### Подготовка пользователя

```java
@Test
void shouldLogin(@User TestUser user) {
  // page — Playwright Page из изолированного браузерного контекста теста.
  new LandingPage(page)
      .open()
      .login()
      .fillLoginForm(user.username(), user.password())
      .signIn()
      .shouldBeAuthenticated()
      .openProfile()
      .shouldBelongTo(user.username());
}
```

`@User` автоматически подключает `UserExtension`. Extension создаёт
`UserFixture`, сохраняет её в JUnit Store и только затем вызывает `create()`.
Записи пользователя и прав `read`/`write` создаются одной транзакцией;
пароль кодируется через Spring Security.

JUnit вызывает `UserFixture.close()` при закрытии Store, в том числе после ошибки
подготовки или теста. Очистка удаляет права и пользователя из auth.
Профиль в БД API, созданный при первом входе, остаётся.
Пользователи из `RegistrationTest` также остаются в БД.

`data/db/Databases` содержит подключения `AUTH` и `API`.
`JDBC` предоставляет `JdbcClient`, выполнение транзакций и чтение SQL из ресурсов.
SQL тестового модуля хранится в `rfr-e2e/src/test/resources/sql/`.

### Настройки тестов

Приоритет: **system property `rfr.<key>` → ENV `RFR_<KEY>` → fallback в коде**.
В имени ENV точки заменяются на подчёркивания. Пустые значения пропускаются.
Gradle передаёт свойства `rfr.*` в JVM тестов.

| System property | ENV | По умолчанию |
| --- | --- | --- |
| `rfr.base.url` | `RFR_BASE_URL` | `http://localhost:3001` |
| `rfr.auth.url` | `RFR_AUTH_URL` | `http://localhost:9001` |
| `rfr.api.url` | `RFR_API_URL` | `http://localhost:8081` |
| `rfr.auth.db.url` | `RFR_AUTH_DB_URL` | `jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC` |
| `rfr.auth.db.username` | `RFR_AUTH_DB_USERNAME` | `root` |
| `rfr.auth.db.password` | `RFR_AUTH_DB_PASSWORD` | Локальный пароль auth |
| `rfr.api.db.url` | `RFR_API_DB_URL` | `jdbc:mysql://localhost:3306/rangiffler-api?serverTimezone=UTC` |
| `rfr.api.db.username` | `RFR_API_DB_USERNAME` | `root` |
| `rfr.api.db.password` | `RFR_API_DB_PASSWORD` | Локальный пароль API |

Пример запуска с system property:

```powershell
./gradlew.bat '-Drfr.base.url=http://localhost:3001' :rfr-e2e:test --tests io.student.rangiffler.test.LoginTest
```

Настройки тестов описаны в [TestConfig.java](rfr-e2e/src/test/java/io/student/rangiffler/config/TestConfig.java).
Подробности инфраструктуры — в [README E2E-модуля](rfr-e2e/README.md).

### Отчёты

- Gradle HTML: `rfr-e2e/build/reports/tests/test/index.html`.
- JUnit XML: `rfr-e2e/build/test-results/test/`.
- Allure results: `rfr-e2e/build/allure-results/`.

После выполнения тестов:

```powershell
./gradlew.bat :rfr-e2e:allureReport
./gradlew.bat :rfr-e2e:allureServe
```

`allureReport` собирает отчёт, `allureServe` запускает его локальный просмотр.
При первом запуске Gradle может скачать необходимые инструменты Allure.

## Правила Page Object

Проектный [skill Page Object](.agents/skills/java-playwright-page-objects/SKILL.md)
фиксирует соглашения:

- `final` классы, композиция общих компонентов вместо наследования.
- Конструктор только сохраняет `Page` и инициализирует `private final` поля.
- `public static String URL` с адресами из `TestConfig`.
- Имена методов отражают бизнес-действие: `fillLoginForm()`, `signIn()`.
- Заполнение формы и отправка — разные действия.
- Методы проверок не меняют состояние и используют web-first assertions Playwright.
- `@Step` для действий и проверок, параметры в Allure, пароли маскируются.
- Методы общей проверки загрузки наподобие `checkPageLoaded()` не используются.

## Частые проблемы

| Симптом | Что проверить |
| --- | --- |
| `ERR_CONNECTION_REFUSED` | Запущен ли нужный сервис и свободен ли его порт |
| MySQL `Access denied` | Совпадают ли реквизиты сервера, Docker-контейнера и `TestConfig`; не задано ли переопределение через ENV |
| Ошибка соединения JDBC при разрешении `@User` | Доступность MySQL и наличие схемы auth после запуска сервера |
| Предупреждение npm `EBADENGINE` | Версию Node.js: зависимости клиента требуют не ниже 22.22.0 |
| Таймаут UI-теста | URL, доступность auth/API, состояние страницы и ошибки браузерной консоли |
| Ошибка OAuth redirect / issuer | Совпадение адресов и использование `localhost` во всех настройках |

Для остановки `bootRun` и Vite нажмите `Ctrl+C` в их терминалах.
Контейнер БД можно остановить командой `docker stop rfr-mysql`; данные останутся в volume.
