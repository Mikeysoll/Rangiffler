---
name: rfr-test-data
description: Готовит и ревьюит тестовые данные и предусловия rfr-e2e — пользователи, друзья, приглашения, SQL объектов строк. Применять к @User, UserRequest, декораторам UserFixture, объектам строк data/db, SQL в src/test/resources/sql и к тестам, которым нужны данные до входа.
---

# Тестовые данные Rangiffler

Код — `rfr-e2e/src/test/java/io/student/rangiffler/{data,fixture,model,annotation,extension}`, SQL — `rfr-e2e/src/test/resources/sql`. Тест владеет своими данными через фикстуры; правила ревью — skill `rfr-test-review`.

## Правила

1. **Данные уникальны и изолированы.** `RandomDataUtils.randomUsername()` создаёт `rfr_` + UUID без дефисов (32 hex), `randomPassword()` генерирует пароль. Общих имён и «пользователя на все тесты» нет; старый префикс `ui_` учитывается при поиске остаточных данных.
2. **Предусловия декларативны:** атрибуты `friends`, `incomeInvitations`, `outcomeInvitations` аннотации `@User` на параметре `TestUser`. Связи не готовятся в теле теста и не вызываются из страниц; ожидаемые пользователи доступны в одноимённых неизменяемых коллекциях `TestUser`.
3. **Владелец — дерево декораторов UserFixture.** `UserRequest.fixture()` оборачивает `SingleUserFixture` в `UserWithFriendsFixture`, `UserWithIncomeInvitations`, `UserWithOutcomeInvitationsFixture`. Store хранит корень до `create()` и закрывает его; декоратор через `TearDown` закрывает своих пользователей в обратном порядке, затем `origin`, включая частичный сбой подготовки.
4. **Новое предусловие — новый декоратор** над `origin` в `UserRequest.fixture()`, не флаг в `SingleUserFixture` и не ветка в `UserExtension`. Связанные пользователи — `SingleUserFixture`; конструктор связи — `Friendship(id, requesterId, addresseeId, status)`, затем `create()`; для исходящего `PENDING` requester — основной пользователь, addressee — приглашённый.
5. **SQL только в объектах строк `data/db`** (`AuthAccount`, `Profile`, `Friendship`, `Friendships`), не в тестах, extension'ах или страницах. Текст хранится в `src/test/resources/sql/{auth,api,common}`; вставка требует ровно одну строку через `JDBC.requireRows(actual, 1, context)`.
6. **API-профиль создаётся явно.** `SingleUserFixture.create()` вызывает `AuthAccount.create()`, затем `Profile.create()` со страной `ru`; связанный пользователь без API-профиля не виден в People.
7. **Атрибуты проверяются до первого SQL:** отрицательный счётчик — `IllegalArgumentException` из конструктора `UserRequest`; `UserRequest(User)` передаёт все три счётчика, поэтому новые атрибуты не требуют ветвления в `UserExtension`.
8. **Состояние читается через `exists()` объектов строк**, не SQL в тестах; каждый атрибут `@User` покрыт мишенью `@ProbeTarget` в `test/extension/UserExtensionTest` через EngineTestKit. Без явного `rfr.probe.enabled=true` мишени отключены.
9. **Пул заранее заведённых пользователей — только там, где создать нельзя** (внешняя система, ручная учётка); цена — общее состояние между тестами. Это правило выбора предусловий, а не утверждение о наличии реализации пула в проекте.

## Проверка результата

После прогона в `rangiffler-auth.user` и `rangiffler-api.user` не остаются тестовые пользователи с буквальным префиксом `rfr_` (также проверяй старые `ui_`), в `rangiffler-api.friendship` — связи с их участием; проверяй известные username через `AuthAccount`, `Profile`, `Friendships` и `exists()`. В SQL LIKE символ `_` означает любой символ: префикс нужно проверять буквально. Назови реально выполненные прогоны; компиляция прогоном не считается.
