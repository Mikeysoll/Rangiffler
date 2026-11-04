# ДЗ03: исправления после независимого ревью

**Вердикт: НЕ ГОТОВО по строгому Definition of Done.** Подтверждённые замечания к коду и покрытию исправлены, новые проверки зелёные. Блокирует сдачу старый остаток обычной БД: два ui_-пользователя в каждой схеме и одна friendship. Их идентификаторы полностью совпадают с предыдущим снимком. Данные вручную не удалялись; первоначальный источник строк не установлен.

Этот отчёт описывает новый проход исправлений и заменяет прежние результаты в HW03_REVIEW.md применительно к текущему коду. Коммиты, push и PR не создавались.

## Исправления

| Файл и строка | Изменение и проверка |
| --- | --- |
| test/extension/UsersQueueExtensionTest.java:72 | complete() после успешных и упавших мишеней сверяет все три исходные пары username/password, уникальность и таймаут четвёртого получения. Assertions возвращают boolean и не печатают пароли. |
| test/fixture/StaticUserFixtureTest.java:113 | Та же проверка исходных пар после таймаута, повторного close, прерывания и конкурентных аренд. |
| test/extension/UserExtensionTest.java:192 | Положительная мишень калибрует наблюдатель create, отрицательная не должна добавлять вызовов. Проверяются IllegalArgumentException, отсутствие исполнения тела, адресный снимок и отсутствие ресурсов после Store. |
| test/extension/probe/UserCreation.java:10; CreationObservation.java; META-INF/aop.xml | Новый тестовый AspectJ-наблюдатель фиксирует вход в SingleUserFixture.create() до тела метода. percflow ограничивает состояние одним управляющим тестом; общего static-списка нет. Положительная калибровка предотвращает ложный успех при неработающем наблюдении. |
| build.gradle | Уже использовавшийся aspectjrt доступен тестовой компиляции; версия не менялась. Наблюдение работает через существующий Gradle javaagent, без изменений production-классов, UserExtension и SingleUserFixture. |
| data/db/UserSnapshot.java:13; SQL select-user-identities.sql и select-friendship-identities.sql | Снимки адресованы известным username. Идентификаторы читаются через HEX(id), а не декодируются из binary(16) в String. SQL остаётся в data/db/resources. |
| test/data/db/UserSnapshotTest.java:29 | Постоянная проверка HEX-формы, кардинальности и независимости от создания постороннего пользователя; проверка изменения/восстановления снимка сохранена. |
| test/auth/LoginTest.java:16; test/auth/RegistrationTest.java:20 | Page передаётся через @UsePlaywright. Ручные Playwright.create/newContext/newPage удалены; исходные сценарии и assertions сохранены. |
| page/PeoplePage.java:91 | После проверки состава кнопок проверяется видимость каждой кнопки в конкретной строке. Waiting остаётся отдельным видимым текстовым индикатором. |
| test/fixture/UserWithOutcomeInvitationsFixtureTest.java:20 | Параметризованное падение после 0, 1 и 2 уже созданных связей. Проверяются очистка аккаунтов/профилей/friendship и закрытие приглашённых до origin. |

Все Java-пути в таблице относительны rfr-e2e/src/test/java/io/student/rangiffler; SQL и aop.xml — rfr-e2e/src/test/resources. Production-приложения rfr-api/rfr-auth не менялись. UserExtension, UserRequest, SingleUserFixture и декораторы для этих исправлений не изменялись.

## Результаты проверок

Восстановлены Docker Desktop, MySQL через Compose с сохранением mysql-volume, auth и API через runAll, фронтенд через npm run dev. Бизнес-логика не менялась ради инфраструктуры.

Все команды использовали JDK 25, Gradle Wrapper, --rerun-tasks --no-daemon --console=plain. Отдельной :rfr-e2e:e2e нет; UI запускается через :rfr-e2e:test.

| Запуск | Результат |
| --- | --- |
| Целевые UserExtensionTest, UserSnapshotTest, UsersQueueExtensionTest, StaticUserFixtureTest, UserWithOutcomeInvitationsFixtureTest | 31/31 успешно после расширения частичных падений |
| :rfr-e2e:test, независимый прогон 1 | 108 записей: 88 выполнены, 20 пропущены; failures=0, errors=0 |
| :rfr-e2e:test, независимый прогон 2 | 108 записей: 88 выполнены, 20 пропущены; failures=0, errors=0 |
| :rfr-e2e:test --tests '*TabTest', прогон 1 | 12/12 успешно |
| :rfr-e2e:test --tests '*TabTest', прогон 2 | 12/12 успешно |

Компиляция и git diff --check успешны. Наблюдение за create действительно включено: положительная калибровка создала и очистила пользователя, вызов остался в наблюдении; отрицательная мишень не добавила ни одного вызова. Негативные значения проверяются до существующей границы создания ресурса, а не только по пустой БД после очистки.

Новых мутаций кода в этом проходе не выполнялось. Прежние mutation-результаты относятся к предыдущей проверке, а не к текущим запускам.

Логи и копии XML находятся в игнорируемом rfr-e2e/build/hw03-fix-audit: full-1, full-2, ui-1, ui-2 и одноимённые .log. Целевой запуск отражён в targeted.log и выводе Gradle текущей сессии; финальное расширение partial-create до трёх случаев проверено дополнительным целевым запуском и обоими полными прогонами.

## БД и DoD

Проверялись буквальные префиксы LEFT(username,3)='ui_' и LEFT(username,4)='rfr_', оба участника friendship и COUNT(DISTINCT f.id).

| Обычная БД | До | После full 1 | После full 2 | После UI и в конце |
| --- | ---: | ---: | ---: | ---: |
| auth.user, ui_ | 2 | 2 | 2 | 2 |
| api.user, ui_ | 2 | 2 | 2 | 2 |
| friendship с участием ui_ | 1 | 1 | 1 | 1 |
| Пользователи и связи rfr_ | 0 | 0 | 0 | 0 |

Сверены также конкретные username/id: финальный набор полностью совпал с предыдущим baseline, а не только по количеству. Новых остаточных данных от текущих прогонов не выявлено. Старые строки не удалялись и не скрывались фильтром.

| Условие | Статус |
| --- | --- |
| Собственные уникальные предусловия, кроме StaticUser | Подтверждено чтением и прогонами |
| Возвращены все три исходные пары без дубликатов | Подтверждено постоянными тестами после успеха, падения и конкурентных аренд |
| Нулевые остатки пользователей и friendship | НЕ ВЫПОЛНЕНО: старые строки сохранены |
| ProbeTarget не запускаются без opt-in | Подтверждено guards в UserExtensionTest и UsersQueueExtensionTest |
| UserExtensionTest без браузера | Тесты не используют Playwright; целевые мишени выполнены через EngineTestKit |
| People: состав, разграничение, кнопки, пустое состояние | Два успешных UI-прогона |
| Очистка частично созданного дерева | Три проверенные стадии: до первой, второй и третьей связи |
| UserExtension/UserRequest не менялись ради пула | Подтверждено архитектурой и diff |

## Финальное независимое ревью

Запущен субагент с нативной ролью test-reviewer: /root/post_fix_review. Он прочитал изменения, но не запускал тесты, чтобы не конкурировать с последовательными прогонами родителя.

Вывод: **«Новых подтверждённых дефектов после исправлений не обнаружено. Замечания предыдущего ревью закрыты в коде и постоянных проверках».**

Ревьюер подтвердил исходные пары пула, централизованное владение браузером, видимость кнопок, адресность и HEX снимков, расширение partial-create и корректное ограничение наблюдателя percflow. Механизм JUnit Store, put до create и симметрия освобождения сохранены.

Границы проверки:

- UserCreation наблюдает существующий SingleUserFixture.create в последовательном EngineTestKit-сценарии. Это не универсальный аудит произвольного SQL и не наблюдение будущего асинхронного создания в другом потоке; таких путей текущий код не содержит.
- Наблюдатель зависит от уже настроенного Gradle AspectJ javaagent. Прямой запуск из IDE должен использовать ту же конфигурацию либо Gradle.
- Не инъецировалась каждая возможная ошибка каждого SQL-оператора. Вместо этого проверены несколько реальных состояний частично созданного дерева и сохранена независимая очистка через TearDown.
- Flaky-сбоев за два полных и два UI-прогона не обнаружено; это не доказательство невозможности нестабильности при любой нагрузке.
- Синхронизация и таймауты статического пула оставлены без изменения: подтверждённого дефекта в них не было.

## Готовность состава PR

Оба skills сохранены: rfr-test-data — 24 строки, rfr-test-review — 25 строк; дополнительное правило 9 о проверке всех исходных пар после успеха и ошибки присутствует. Необязательный Relation не реализовывался.

Новые Java/SQL-файлы и META-INF/aop.xml проверены через git ls-files/check-ignore: они не игнорируются, но ещё untracked. Перед будущим коммитом нужно включить перечисленный ниже набор вместе с tracked-изменениями ДЗ. Не использовать безадресный git add .: .playwright-mcp, существующие .worktrees и диагностические build-артефакты к PR не относятся. Пары pool_* — намеренные фиктивные данные задания, а не реальные секреты стенда. В отчёты и assertion-сообщения пароли не добавлены.

Коммит, PR и индексирование файлов автоматически не выполнялись. Предшествующие пользовательские изменения сохранены.

### Новые исходные файлы для включения
- `rfr-e2e/src/test/java/io/student/rangiffler/annotation/StaticUser.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/data/db/UserSnapshot.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/extension/UsersQueueExtension.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/fixture/StaticUserFixture.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/fixture/UserWithOutcomeInvitationsFixture.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/data/db/UserSnapshotTest.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/extension/UsersQueueExtensionTest.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/extension/probe/CreationObservation.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/extension/probe/UserCreation.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/fixture/StaticUserFixtureTest.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/fixture/UserWithOutcomeInvitationsFixtureTest.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/people/FriendsTabTest.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/people/IncomeInvitationsTabTest.java`
- `rfr-e2e/src/test/java/io/student/rangiffler/test/people/OutcomeInvitationsTabTest.java`
- `rfr-e2e/src/test/resources/META-INF/aop.xml`
- `rfr-e2e/src/test/resources/sql/api/select-friendship-identities.sql`
- `rfr-e2e/src/test/resources/sql/common/select-user-identities.sql`

## Что осталось до сдачи

1. Установить происхождение и владельца старых двух ui_-пользователей и одной связи; согласовать допустимое обращение с этими данными. Ручная очистка ради зелёного DoD не выполнялась.
2. Включить исходные файлы и новый отчёт в будущий коммит после проверки состава. Коммит и PR требуют отдельного запроса.
3. Не объявлять строгий DoD выполненным, пока нулевые остатки обычной БД не подтверждены.
