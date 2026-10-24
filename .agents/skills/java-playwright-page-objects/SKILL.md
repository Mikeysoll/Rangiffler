---
name: java-playwright-page-objects
description: Create, update, or review Java Playwright Page Objects and reusable UI components with Allure steps. Apply when implementing PO classes or adapting UI tests to them, especially in Rangiffler.
---

# Java Playwright Page Objects

Применяй эти правила при создании и изменении Page Object (PO) на Java с Playwright и Allure. Не переписывай существующие PO вне запрошенной задачи. Явные указания пользователя имеют приоритет.

## Структура

- Объявляй классы страниц и переиспользуемых UI-компонентов `public final class`.
- Используй композицию: PO хранит `private final Page`, локаторы и объекты общих компонентов (например, Header, Sidebar, Form). Не создавай BasePage и не используй наследование для переиспользования функциональности.
- Храни локаторы в PO/компоненте, не выноси их в тест. Выбирай устойчивые локаторы по роли, label или placeholder, уточняй неоднозначные имена.
- Конструктор PO принимает `Page page` и выполняет только присвоение `this.page = page` и инициализацию `private final` полей PO (локаторов и компонентов). Не выполняй в конструкторе навигацию, действия с UI, проверки, ожидания или другую бизнес-логику.
- Для каждой страницы объявляй именно `public static String URL`, например `public static String URL = TestConfig.FRONT_URL + "/profile";`. Базовые адреса бери из `config/TestConfig`, не дублируй их в PO. Компонентам без собственной страницы URL не нужен.

## Методы

- Не создавай методы `checkPageLoaded` и их аналоги (`waitForPageLoaded`, `ensureLoaded` и т. п.) для общей проверки готовности страницы. Используй web-first assertions Playwright в конкретных проверках ожидаемого поведения; они сами ожидают выполнения условия. Не добавляй отдельную проверку загрузки перед каждым действием или assertion.
- Каждый публичный метод бизнес-действия и каждый метод проверки помечай `@Step` с понятным описанием. Конструкторы и технические приватные помощники не являются самостоятельными шагами.
- Логгируй переданные бизнес-параметры в Allure: называй аргументы осмысленно, отражай важные значения в шаблоне `@Step("Заполнить форму пользователя {username}")`; используй параметры шага Allure для остальных значений. Не добавляй второй логгер только ради дублирования Allure. Пароли и токены маскируй/исключай из отчёта, не помещай их в текст шага.
- Называй методы по конкретной форме и бизнес-действию: `fillRegistrationForm(...)`, `fillLoginForm(...)`, `fillProfileForm(...)`, `registerUser()`, `signIn()`, `saveProfile()`. По имени должно быть понятно назначение метода без чтения реализации. Не используй общие имена `fillForm()` и `submit()` для публичных бизнес-методов PO; это лишь условные примеры разделения ответственности.
- Объединяй операции одного бизнес-действия в один метод: например, `fillRegistrationForm(username, password, confirmation)` заполняет всю форму регистрации. Не заставляй тест последовательно вызывать отдельные сеттеры каждого поля.
- Разделяй заполнение формы и отправку: `fillRegistrationForm(...)` только заполняет поля, `registerUser()` нажимает кнопку регистрации. Возвращай текущий PO для цепочек действий, а при переходе — PO целевой страницы.
- Методы проверки (`should...`, `check...`) содержат только проверки. Запрещены click/fill/press, навигация, перезагрузка, создание данных и скрытая подготовка состояния. Нужную навигацию выполняй отдельным методом до вызова проверки. Возврат `this` допустим.
- Если пользователь не указал иной способ, проверки выполняй через Playwright conditions: `PlaywrightAssertions.assertThat(...)` и `isVisible()`, `hasText()`, `hasValue()`, `hasURL()` и т. п. Используй встроенное ожидание assertions; не подменяй его `assertTrue(locator.isVisible())`, `Thread.sleep()` или ручным polling.

## Пример разделения действий и проверок

```java
public final class ProfilePage {
  public static String URL = TestConfig.FRONT_URL + "/profile";

  private final Page page;
  private final Locator firstName;
  private final Locator surname;
  private final Locator save;

  public ProfilePage(Page page) {
    this.page = page;
    firstName = page.getByLabel("First Name");
    surname = page.getByLabel("Surname");
    save = page.getByRole(AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName("Save").setExact(true));
  }

  @Step("Открыть профиль")
  public ProfilePage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Заполнить профиль: имя {name}, фамилия {lastName}")
  public ProfilePage fillProfileForm(String name, String lastName) {
    firstName.fill(name);
    surname.fill(lastName);
    return this;
  }

  @Step("Сохранить профиль")
  public ProfilePage saveProfile() {
    save.click();
    return this;
  }

  @Step("Проверить имя профиля: {expectedName}")
  public ProfilePage shouldHaveFirstName(String expectedName) {
    assertThat(firstName).hasValue(expectedName);
    return this;
  }
}
```

Используй импорты Playwright `Page`, `Locator`, `AriaRole`, Allure `Step` и статический импорт `PlaywrightAssertions.assertThat`.

## Проверка результата

Проверь `final`, отсутствие наследования, композицию общих компонентов, `URL`, `@Step` и логгирование параметров. Убедись, что проверки не изменяют состояние, а тест читабельно описывает бизнес-сценарий. Скомпилируй изменения и запусти соответствующий UI-тест, если среда приложения доступна; иначе явно укажи, что осталось непроверенным. Создание skill само по себе не требует изменения существующих PO или запуска приложения.
