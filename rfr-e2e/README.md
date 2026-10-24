# UI-тесты

Для запуска нужны JDK 25, MySQL и работающие `rfr-auth` (9001), `rfr-api` (8081),
`rfr-gql-client` (3001). Команда из корня проекта:

```powershell
./gradlew.bat :rfr-e2e:test --tests io.student.rangiffler.test.RegistrationTest
```

Тест запускает Chromium с видимым окном в новом изолированном контексте. Playwright
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
