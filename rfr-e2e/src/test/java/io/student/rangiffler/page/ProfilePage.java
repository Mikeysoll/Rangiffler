package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.Header;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {
  public static String URL = TestConfig.FRONT_URL + "/profile";

  private final Page page;
  private final Locator usernameInput;
  private final Header header;

  public ProfilePage(Page page) {
    this.page = page;
    usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username").setExact(true));
    header = new Header(page);
  }

  @Step("Проверить, что авторизован пользователь {username}")
  public ProfilePage shouldBelongTo(String username) {
    assertThat(page).hasURL(URL);
    assertThat(usernameInput).hasValue(username);
    header.shouldAllowLogout();
    return this;
  }
}
