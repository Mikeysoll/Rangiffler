---
name: rfr-test-review
description: Ревью тестового кода rfr-e2e на владение ресурсами теста. Применять к diff'ам в extension/, fixture/ и data/, новым предусловиям @User и декораторам UserFixture, разбору падений с подозрением на утечку данных или общее состояние.
---

# Ревью владения ресурсами rfr-e2e

Ресурс — всё, что тест получает и обязан вернуть: строки в БД, `Page`, `BrowserContext`. Для каждого ресурса найди владельца, момент получения, место хранения и момент освобождения.

1. **Владелец — дерево декораторов.** В Store лежит только корень `UserFixture` из `UserRequest.fixture()`, Store закрывает его как `AutoCloseable`. Декораторы `UserWithFriendsFixture` и `UserWithIncomeInvitations` в `fixture/` владеют `origin` и списками `friends` и `inviters` соответственно; поля extension'а, статика и `ThreadLocal` ресурсами не владеют.
2. **Регистрация до частичной подготовки.** `put` в Store выполняется до `create()`. `create()` не атомарен (`AuthAccount`, `Profile`, партнёры, `Friendship`), поэтому `close()` обязан работать после падения на любом шаге.
3. **Симметрия create/close.** Декораторы закрывают `friends.reversed()` или `inviters.reversed()`, затем `origin` через `TearDown`, который выполняет независимые шаги и сохраняет ошибки. `SingleUserFixture.close()` удаляет `Profile`, затем `AuthAccount` по `username`; `Profile.delete()` сначала удаляет связи через `Friendships(username)`, затем зависимые строки и профиль. Удаляй ровно созданные ресурсы, без широких `DELETE`/`TRUNCATE`.
4. **Новое предусловие — новый декоратор.** Композиция находится в `UserRequest.fixture()`, а не во флаге `SingleUserFixture` и не в ветке `UserExtension`. Extension знает только `UserRequest` и `UserFixture`.
5. **Один @User — один параметр TestUser.** Каждому `@User` соответствует один параметр `TestUser` и один ключ Store — `parameterContext.getParameter()` в пространстве имён текущего вызова теста. Связанные пользователи принадлежат дереву фикстур (`friends`, `inviters`), а не отдельным параметрам; `TestUser` содержит только `id`, `username`, `password`, связи доступны через `Friendships(username)`.
6. **Браузерными ресурсами владеет @UsePlaywright.** `Page` и `BrowserContext` не создаются и не закрываются в тестах и extension'ах.
7. **Слои.** В `extension/` и `fixture/` нет SQL и локаторов; в `data/` нет JUnit, `UserRequest` принимает проектную аннотацию `annotation.User`. SQL-операции находятся в `data/db`, SQL-файлы — в `src/test/resources/sql`; `db` не зависит от вышележащих моделей `data`. В сценариях `test/` нет локаторов и SQL; проверки строк через объекты `data/db` в контрактных пробах допустимы.
8. **Каждый вывод подтверждён.** Подтверждай вывод пробой в `test/extension/UserExtensionTest` (EngineTestKit, `exists()` из `db`, цели `Observed` и `Failing`) или конкретной строкой кода. Без найденной причины изменений не предлагай.

## Формат ответа

- Для каждого ресурса: владелец, получение, хранение, освобождение.
- Для каждого замечания: файл и строка, сценарий сбоя, как воспроизвести (какая проба или какой падающий шаг `create()`).
- Три списка: «подтверждено запуском», «по чтению кода», «не проверено».
- Если дефектов нет, так и скажи и перечисли ограничения решения.
