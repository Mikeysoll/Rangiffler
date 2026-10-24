package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MyTravelsPage {
  private final Page page;

  public MyTravelsPage(Page page) {
    this.page = page;
  }

  @Step("Проверить вход в личный кабинет")
  public MyTravelsPage shouldBeOpened() {
    assertThat(page).hasURL(Pattern.compile(".*/my-travels$"));
    assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("Travels map").setExact(true))).isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Logout").setExact(true))).isVisible();
    return this;
  }

  @Step("Открыть профиль")
  public ProfilePage openProfile() {
    page.getByRole(AriaRole.BANNER).getByRole(AriaRole.LINK,
        new Locator.GetByRoleOptions().setName("Profile").setExact(true)).click();
    return new ProfilePage(page);
  }
}
