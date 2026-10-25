package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.Header;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class MyTravelsPage {
  public static String URL = TestConfig.FRONT_URL + "/my-travels";

  private final Page page;
  private final Header header;
  private final Locator main;
  private final Locator people;

  public MyTravelsPage(Page page) {
    this.page = page;
    header = new Header(page);
    main = page.getByRole(AriaRole.MAIN);
    people = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("People").setExact(true));
  }

  @Step("Проверить успешную авторизацию на странице путешествий")
  public MyTravelsPage shouldBeAuthenticated() {
    assertThat(page).hasURL(URL);
    header.shouldAllowLogout();
    return this;
  }

  @Step("Проверить видимость основных элементов главной страницы")
  public MyTravelsPage checkThatMainElementsVisible() {
    assertThat(main.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName("Travels map").setExact(true))).isVisible();
    assertThat(main.getByRole(AriaRole.GROUP, new Locator.GetByRoleOptions().setName("Travels filter").setExact(true))).isVisible();
    assertThat(main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Only my travels").setExact(true))).isVisible();
    assertThat(main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("With friends").setExact(true))).isVisible();
    assertThat(main.getByRole(AriaRole.IMG, new Locator.GetByRoleOptions().setName("World map").setExact(true))).isVisible();
    assertThat(main.getByRole(AriaRole.HEADING, new Locator.GetByRoleOptions().setName("Photos").setExact(true))).isVisible();
    assertThat(main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add photo").setExact(true))).isVisible();
    header.shouldAllowLogout();
    return this;
  }

  @Step("Открыть профиль")
  public ProfilePage openProfile() {
    return header.openProfile();
  }

  @Step("Открыть список пользователей")
  public PeoplePage openPeople() {
    people.click();
    return new PeoplePage(page);
  }
}
