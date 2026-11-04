package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

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

  @Step("Отправить форму с неверными учётными данными")
  public LoginPage submitInvalidCredentials() {
    submitButton.click();
    return this;
  }

  @Step("Проверить ошибку Bad credentials")
  public LoginPage checkThatBadCredentials() {
    assertThat(page.locator(".form__error")).isVisible();
    assertThat(page.locator(".form__error")).hasText("Bad credentials");
    return this;
  }
}
