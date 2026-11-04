package io.student.rangiffler.page.component;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.page.ProfilePage;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class Header {
  private final Page page;
  private final Locator profileLink;
  private final Locator logoutButton;

  public Header(Page page) {
    this.page = page;
    Locator banner = page.getByRole(AriaRole.BANNER);
    profileLink = banner.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Profile").setExact(true));
    logoutButton = banner.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Logout").setExact(true));
  }

  @Step("Открыть профиль через шапку")
  public ProfilePage openProfile() {
    profileLink.click();
    return new ProfilePage(page);
  }

  @Step("Проверить доступность выхода из аккаунта")
  public Header shouldAllowLogout() {
    assertThat(logoutButton).isVisible();
    return this;
  }

  @Step("Выйти из аккаунта")
  public LandingPage logout() {
    logoutButton.click();
    page.waitForURL(TestConfig.FRONT_URL + "/");
    return new LandingPage(page);
  }
}
