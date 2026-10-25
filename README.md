# Rangiffler

> **Приватный репозиторий.** Здесь доступен исходный код из видеоуроков курса
> **QA.GURU Java Advanced (Playwright)**.
>
> Исходно репозиторий является копией [qa-guru/rfr](https://github.com/qa-guru/rfr).


# Текущая структура проекта

```
rfr/
├── rfr-auth/              # OAuth 2.0 Authorization Server (готов)
├── rfr-api/               # GraphQL API Backend (базовая реализация)
│   ├── Mock контроллеры для фотографий
│   ├── Реальные контроллеры для пользователей и стран
│   └── Flyway миграции БД
├── rfr-gql-client/        # React + TypeScript Frontend (готов)
└── rfr-e2e/               # E2E тесты: JUnit 6 + Playwright + Allure 3
```

**Порты:**
- rfr-auth: 9001
- rfr-api: 8081
- rfr-gql-client: 3001

**Технологии:**
- Сборка: Java 25, Gradle 9.7.1
- Backend: Spring Boot 4.1.1, Spring for GraphQL, Spring Security, JPA, Flyway
- Frontend: React 19, TypeScript 6, Vite 8, Apollo Client 4, Material UI 9, React Router 8
- Auth: Spring Authorization Server
- БД: MySQL 8.4
- E2E: JUnit 6, Playwright Java 1.63, Allure 3


#### 1. Запусти зависимости (БД):

```bash
docker compose up -d --wait
```

MySQL 8.4.7 поднимается из `compose.yaml` (контейнер `rfr-mysql`, порт `3306`, пользователь `root`, пароль `secret`, данные в volume `mysql-volume`), команда ждёт статуса `healthy`. Остановить: `docker compose stop`, удалить контейнер с сохранением данных: `docker compose down`. Команда `docker compose down -v` удаляет и данные.

Если контейнер `rfr-mysql` ранее создан через `docker run`, перед первым запуском Compose выполни `docker stop rfr-mysql` и `docker rm rfr-mysql`. Существующие данные в `mysql-volume` сохранятся и будут использованы Compose.

#### 2. Запусти rfr-auth и rfr-api:

```bash
./gradlew runAll
```

Задача запускает `bootRun` обоих сервисов параллельно, Ctrl+C останавливает оба. По отдельности: `./gradlew :rfr-auth:bootRun` и `./gradlew :rfr-api:bootRun`, или main class из IDE.

Auth доступен на порту 9001: http://localhost:9001, rfr-api — на порту 8081: http://localhost:8081/graphql

#### 3. Обнови зависимости и запускай фронт:

```bash
cd rfr-gql-client
npm ci
npm run dev
```

Фронт стартанет на порту 3001: http://localhost:3001/

В текущей локальной БД подготовлен тестовый пользователь:

- Логин: `duck`
- Пароль: `Duck4829`

Для входа нажми «Login». При создании новой БД этот аккаунт автоматически не создаётся.

#### 4. Прогони E2E-тесты:

```bash
./gradlew e2e
```

Подробности — `rfr-e2e/README.md`.

#### 5. Проверь работоспособность

Кнопка "Login" работает через сервис auth. После успешной авторизации ты попадешь на главную страницу Rangiffler.

Mock контроллеры в rfr-api будут возвращать статические данные из JSON файлов для ленты фотографий.
Реальная работа с пользователями и странами уже реализована через базу данных.
