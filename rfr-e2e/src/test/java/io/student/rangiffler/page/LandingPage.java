package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;

public final class LandingPage {
  public static String URL = TestConfig.FRONT_URL;

  private final Page page;
  private final Locator registerLink;
  private final Locator loginButton;

  public LandingPage(Page page) {
    this.page = page;
    registerLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Register").setExact(true));
    loginButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login").setExact(true));
  }

  @Step("Открыть главную страницу")
  public LandingPage open() {
    page.navigate(URL);
    return this;
  }

  @Step("Перейти к регистрации")
  public RegistrationPage register() {
    registerLink.click();
    return new RegistrationPage(page);
  }

  @Step("Перейти к входу")
  public LoginPage login() {
    loginButton.click();
    return new LoginPage(page);
  }
}
