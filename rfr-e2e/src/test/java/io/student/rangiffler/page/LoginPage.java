package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.student.rangiffler.config.TestConfig;

public final class LoginPage {
  public static String URL = TestConfig.AUTH_URL + "/login";

  private final Page page;
  private final Locator usernameInput;
  private final Locator passwordInput;
  private final Locator submitButton;

  public LoginPage(Page page) {
    this.page = page;
    usernameInput = page.getByPlaceholder("Type your username");
    passwordInput = page.getByPlaceholder("Type your password");
    submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  @Step("Заполнить форму входа пользователя {username}")
  public LoginPage fillLoginForm(String username, @Param(mode = Parameter.Mode.MASKED) String password) {
    usernameInput.fill(username);
    passwordInput.fill(password);
    return this;
  }

  @Step("Отправить форму входа")
  public MyTravelsPage signIn() {
    submitButton.click();
    return new MyTravelsPage(page);
  }
}
