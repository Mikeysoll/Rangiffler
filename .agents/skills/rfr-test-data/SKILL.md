---
name: rfr-test-data
description: Готовит и ревьюит тестовые данные и предусловия rfr-e2e — пользователи, друзья, приглашения, SQL объектов строк. Применять к @User, UserRequest, декораторам UserFixture, объектам строк data/db, SQL в src/test/resources/sql и к тестам, которым нужны данные до входа.
---

# Тестовые данные Rangiffler

Код — `rfr-e2e/src/test/java/io/student/rangiffler/data` и `rfr-e2e/src/test/resources/sql`. Данные тест создаёт и удаляет сам; правила владения — skill `rfr-test-review`.

## Правила

1. **Данные уникальны.** Username = `rfr_` + 8 hex (`RandomDataUtils.randomUsername()`), пароль генерируется. Общих имён и «пользователя на все тесты» нет.
2. **Предусловия декларативны:** `@User(friends, incomeInvitations, outcomeInvitations)` на параметре `TestUser`. Связи не готовятся в теле теста и не вызываются из страниц.
3. **Владелец — дерево декораторов.** `UserRequest.fixture()` собирает `SingleUserFixture` → `UserWith<X>Fixture`; каждый декоратор удаляет своих пользователей, потом `origin` (`TearDown`). Тест не удаляет данные, которые не создавал.
4. **Новое предусловие — новый декоратор** над `origin` в `UserRequest.fixture()`, не флаг в `SingleUserFixture` и не ветка в `UserExtension`. Связанные пользователи — `SingleUserFixture`, связь — `new Friendship(requester, addressee, status).create()`.
5. **SQL только в объектах строк `data/db`** (`AuthAccount`, `ApiProfile`, `Friendship`, `Friendships`): `create`/`delete`/`exists`, текст в `resources/sql/{auth,api}`. Вставка требует ровно одну строку (`Jdbc.requireOneRow`).
6. **API-профиль создаётся явно** (`ApiProfile`, страна `ru`): связанный пользователь без профиля не виден в People.
7. **Атрибуты проверяются до первого SQL:** отрицательный счётчик — `IllegalArgumentException` из конструктора `UserRequest`.
8. **Состояние читается через `exists()` объектов строк**, не SQL в тестах; каждый атрибут `@User` покрыт мишенью `@ProbeTarget` в `UserExtensionTest`.
9. **Пул заранее заведённых пользователей — только там, где создать нельзя** (внешняя система, ручная учётка); цена — общее состояние между тестами.

## Проверка результата

После прогона в `rangiffler-auth.user`, `rangiffler-api.user` и `rangiffler-api.friendship` нет строк `rfr_%`. Назови реально выполненные прогоны; компиляция прогоном не считается.