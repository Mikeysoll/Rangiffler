# GIGACODE.md

Инструкции для агентских CLI при работе в репозитории **Rangiffler** (проект `rfr`).

## Что это за проект

Rangiffler — дипломная работа QA.GURU Advanced: соцсеть для путешествий (фотолента, дружба, лайки, статистика по странам).
Репозиторий сочетает **готовый React-фронтенд**, **рабочий сервис авторизации** и **базовый GraphQL-бэкенд с mock-слоем** —
цель диплома — довести бэкенд до микросервисов и покрыть всё тестами.

Ключевые требования диплома:
1. Микросервисный бэкенд (Spring Boot).
2. Полноценное тестовое покрытие сервисов и фронтенда (unit-тесты — большой плюс).
3. Оформленный README, по которому всё реально запускается.

## Структура репозитория

```
rfr/
├── build.gradle            # корневая сборка: Java 25 toolchain, -parameters, Boot BOM 4.1.1
├── settings.gradle         # modules: rfr-api, rfr-auth, rfr-e2e
├── gradlew / gradlew.bat   # Gradle 9.7.1 wrapper
├── localenv.sh             # MySQL 8.4.7 в Docker
├── ports.sh                # убить процессы на портах 9001 / 8081 / 3001
├── rfr-auth/               # OAuth 2.1 / OIDC Authorization Server  → :9001
├── rfr-api/                # GraphQL API (Resource Server + DGS)    → :8081
├── rfr-gql-client/         # React 19 + TS 6 + Vite 8 SPA          → :3001
└── rfr-e2e/                # E2E-каркас: JUnit 6 + Playwright Java + Allure 3
```

Порты фиксированы: `9001` auth, `8081` api, `3001` frontend. Они же зашиты в `application*.y*ml`, CORS, redirect URI и `ports.sh`.

## Технологический стек

| Слой | Технологии |
|---|---|
| Сборка | Java 25 (toolchain), Gradle 9.7.1, Spring Boot 4.1.1 (Jackson 3!), Spring Security 7 |
| Backend | Spring for GraphQL + DGS Codegen 8.7.0, Spring Data JPA/Hibernate, Flyway 13.7.0, MySQL 8.4 (драйвер 8.0.33), Lombok 1.18.48, Thymeleaf (auth) |
| Frontend | React 19, TypeScript 6, Vite 8, Apollo Client 4, MUI 9, React Router 8, ESLint 10 |
| E2E | JUnit 6.1.3, Playwright Java 1.63.0, Allure Java 3.0.0 / CLI 3.17.0, AspectJ weaver как `-javaagent` |

## Команды

### Инфраструктура
```bash
bash localenv.sh    # MySQL 8.4.7 в Docker (root/secret, порт 3306)
bash ports.sh       # освободить 9001/8081/3001
```
**Осторожно:** `localenv.sh` сначала делает `docker stop $(docker ps -a -q)` и `docker rm` — удаляет **все** контейнеры на машине. Не запускать без явного согласия пользователя.

### Запуск сервисов (порядок важен)
```bash
cd rfr-auth && ../gradlew bootRun        # :9001 — нужен для JWKS rfr-api
cd rfr-api  && ../gradlew bootRun        # :8081, GraphiQL: /graphiql
cd rfr-gql-client && npm i && npm run dev # :3001
```

### Сборка и тесты
```bash
./gradlew build                    # все модули
./gradlew :rfr-api:build           # только API
./gradlew :rfr-api:generateJava    # регенерация DGS-моделей из схемы
./gradlew :rfr-e2e:test            # E2E (JUnit 6 + Playwright)
./gradlew allureReport             # отчёт Allure
```

### Фронтенд
```bash
cd rfr-gql-client
npm run dev      # dev-сервер
npm run build    # tsc + vite build (используется как проверка типов)
npm run lint     # eslint --max-warnings 0
npm run preview
```

### Проверка API вручную
`access_token` после логина лежит в `localStorage` фронтенда под ключом `access_token`.
```bash
curl -X POST http://localhost:8081/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <access_token>" \
  -d "{\"query\":\"{ user { id username location { code } } }\"}"
```

## Архитектура и контракты

### rfr-auth (`:9001`)
Spring Authorization Server, OAuth 2.1 / OIDC, Authorization Code + PKCE.
- Публичный клиент `client`, redirect `http://localhost:3001/authorized`, post-logout `http://localhost:3001/logout`, scopes `openid profile`.
- **access token** (JWT): `sub` = username, `aud: ["rangiffler-api"]`, claim `authorities` (`read`/`write`), TTL 10 минут (`AccessTokenCustomizer`).
- **id_token**: `aud` = `client`, TTL 30 минут; используется фронтом только как `id_token_hint` при logout.
- Ключ подписи RSA генерируется при старте — после рестарта `rfr-auth` все токены невалидны.
- Logout: `POST /oauth2/revoke` (RFC 7009, публичный клиент опознаётся по `client_id`) → `/connect/logout` → очистка `JSESSIONID`/`XSRF-TOKEN`.
- БД `rangiffler-auth`: таблицы `user`, `authority`; миграция `db/migration/rangiffler-auth/V1__schema_init.sql`.
- OAuth-авторизации/ключи — in-memory, теряются при рестарте.

### rfr-api (`:8081`)
Spring for GraphQL + DGS Codegen.
- Схема — единственный источник правды: `rfr-api/src/main/resources/graphql/query.graphqls`.
- DGS Codegen генерирует Java-модели (`io.student.rangiffler.model.*`, types в `io.student.rangiffler.model.types`) в `build/generated/sources/dgs-codegen`. **Руками эти классы не пишем.**
- `typeMapping` в `rfr-api/build.gradle`: `UserConnection`/`PhotoConnection` → `Page<...>`, `Date` → `java.time.LocalDate`.
- Security: `POST /graphql` пропускается фильтрами, доступ — через `@PreAuthorize` (`read` для query, `write` для mutation). Проверяются подпись, `iss`, `exp` и **audience** `rangiffler-api` (JWKS `rfr-auth`). `id_token` отклоняется с 401.
- Пагинация: контроллеры принимают `@Argument("page") @Nullable Integer page`, `size`, опциональный `searchQuery` и возвращают `Page<T>` / `Slice<T>`.
- Профиль API создаётся при первом `Query.user` (страна `ru`). UUID в auth и api независимы, связь только по `username`.

Слои: `config/`, `controller/`, `service/api` + `service/impl`, `data/entity|repository|projection`, `exception/`, `util/`.

**Статус реализации:**

| Возможность | Состояние |
|---|---|
| `Query.user`, `Mutation.user` (профиль) | БД |
| `Query.countries` (238 стран из миграции) | БД |
| `Query.users` (поиск, пагинация, сортировка по username) | БД |
| Дружба (`friends`, `incomeInvitations`, `outcomeInvitations`, `Mutation.friendship`) | БД |
| `Query.feed`, `Feed.photos`, `Feed.stat`, `Photo.likes` | **mock** (`resources/mock/*.json`) |
| `Mutation.photo`, `Mutation.deletePhoto` | **mock**, ничего не сохраняет |

Таблицы `photo`, `like`, `photo_like`, `statistic` уже в миграции, но не используются — это и есть задача диплома.

### rfr-gql-client (`:3001`)
Готовый SPA: Apollo Client 4, MUI 9, React Router 8. Токены — `access_token`, `id_token` в localStorage. Текст ошибок GraphQL показывается в снэкбаре только для `BAD_REQUEST` и `NOT_FOUND` (`src/api/graphqlError.ts`).

### rfr-e2e
Каркас: `rfr-e2e/src/test/java/io/student/rangiffler/test/fake/SimpleTest.java`, `src/test/resources/junit-platform.properties`. Allure-плагин 4.3.0, отчёты через `allure { version = ... }`.

## Правила домена

### Дружба
- Строго **одна строка** на пару пользователей в `friendship`: `requester`, `addressee`, статус `PENDING` / `ACCEPTED`.
- Уникальность неупорядоченной пары — на уровне БД через генерируемые колонки `least/greatest` + `unique (user_low_id, user_high_id)` и `check (requester_id <> addressee_id)`.
- `FriendshipEntity` — самостоятельный агрегат: `@ManyToOne(LAZY)` без каскадов, `@Version` (optimistic lock), переходы в методах сущности (`request(...)`, `accept(actor)`, `assertCanDecline(actor)`).
- `ADD` — любой, если пары нет → `PENDING`/`INVITATION_SENT`; `ACCEPT`/`REJECT` — только адресат; `DELETE` — любая сторона, строка удаляется.
- Конфликты (`DataIntegrityViolationException`, `OptimisticLockingFailureException`) превращаются в `FriendshipActionException` (`BAD_REQUEST`), а не в `INTERNAL_ERROR`. Для этого используется `saveAndFlush` и явный `flush()`.

### Ошибки GraphQL
HTTP всегда 200, ошибки в массиве `errors`. Классификация в `GraphQlExceptionResolver`:

| Исключение | `extensions.classification` |
|---|---|
| `ResourceNotFoundException` | `NOT_FOUND` |
| `FriendshipActionException` | `BAD_REQUEST` |
| `IllegalArgumentException` | `BAD_REQUEST` |
| прочие | `INTERNAL_ERROR` (без деталей) |

Новые сервисы обязаны придерживаться этого контракта — по `classification` проверяются негативные e2e-сценарии.

## Соглашения разработки

### Backend
- Пагинацию получать **одним запросом** из JPA: методы репозитория с `Pageable`, `Page<T>` / `Slice<T>`, затем `.map(...)` в GraphQL-тип.
- Списки людей со статусом дружбы — projection `UserWithStatus` через JPQL `select new` (без N+1): страна и статус приходят в одной строке. Методы дублируются в двух вариантах — с `searchQuery` и без.
- `isOwner` для фото — на уровне SQL через интерфейсную проекцию (`FeedPhotoView`).
- Даты — только `java.time` (`LocalDate`, `LocalDateTime`), не `java.util.Date`.
- `@Argument("name")` указывать явно; `page`/`size` — `@Nullable Integer`.
- Методы с `@SchemaMapping` **никогда** не вызывать вручную — их вызывает Spring по запросу клиента.
- `{Typename}Connection` в схеме не описываются и в IDEA подсвечиваются красным — это ожидаемо.
- Время жизни объектов в JSON mock-файлах: мутации ничего не меняют, после обновления страницы данные те же.

### Окружение
- **Spring Boot 4 / Jackson 3**: пакеты `tools.jackson.databind.*`, не `com.fasterxml.jackson.databind.*`.
- **HQL в `@Query` проверяется только при старте приложения** (нужна БД). `./gradlew build` синтаксические ошибки JPQL не поймает — проверять запуском.
- Компиляция обязана идти с `-parameters` (уже настроено в корневом `build.gradle`); сборка из VS Code в `bin/main` его не добавляет и приложение падает на старте.
- `ddl-auto: none`, схема только через Flyway; `open-in-view: false`.
- Логирование: `org.springframework.web` и `security` на DEBUG.

### Тестирование
- Ориентир — Niffler, но покрытие Niffler считается «слабым»: для диплома нужно существенно больше (30+ позитивных e2e-сценариев — это минимум, плюс API-тесты).
- Целевая структура тестового «фреймворка»: аннотация `@ApiLogin(user = @User(photos = @Photo(country = RUSSIA)))` с готовыми прекондишенами (пользователи, партнёры со статусами `FRIEND`/`INCOME_INVITATION`/`OUTCOME_INVITATION`, фото по классам изображений).
- Нужны API-логин и API-создание прекондишенов; E2E — через Playwright.
- Негативные сценарии — по `errors[].extensions.classification`.
- Контроль доступа (чужое фото, повторный лайк, редактирование чужого) — обязательная часть проверок.

## Git
- Основная ветка: `master`, работа идёт через PR (последние коммиты — merge PR #6, #7).
- Не коммитить и не пушить без явной просьбы; коммитить только относящиеся к задаче файлы.

## Полезные факты для отладки
- 401 от `rfr-api` на «валидный» токен → передан `id_token` (`aud` = `client`) либо токен старше 10 минут.
- После рестарта `rfr-auth` — все выданные токены невалидны (новый ключ подписи), нужен повторный логин.
- `GET /login` без сохранённого OAuth-запроса редиректит на фронт — вход начинать с `/oauth2/authorize`.
- CORS настроен на `rangiffler-front.base-uri` (`http://localhost:3001`); при смене порта фронта менять в `application.yml` обоих сервисов.
- GraphiQL доступен, но запросы в нём требуют заголовок `Authorization: Bearer <access_token>`.
