package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Param;
import io.qameta.allure.Step;
import io.qameta.allure.model.Parameter;
import io.student.rangiffler.config.TestConfig;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class RegistrationPage {
  public static String URL = TestConfig.AUTH_URL + "/register";

  private final Page page;
  private final Locator usernameInput;
  private final Locator passwordInput;
  private final Locator confirmationInput;
  private final Locator submitButton;
  private final Locator successMessage;
  private final Locator signInLink;

  public RegistrationPage(Page page) {
    this.page = page;
    usernameInput = page.getByPlaceholder("Type your username");
    passwordInput = page.getByPlaceholder("Type your password", new Page.GetByPlaceholderOptions().setExact(true));
    confirmationInput = page.getByPlaceholder("Repeat your password");
    submitButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up").setExact(true));
    successMessage = page.getByText("Congratulations! You've registered!", new Page.GetByTextOptions().setExact(true));
    signInLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in").setExact(true));
  }

  @Step("Заполнить форму регистрации пользователя {username}")
  public RegistrationPage fillRegistrationForm(String username,
      @Param(mode = Parameter.Mode.MASKED) String password,
      @Param(mode = Parameter.Mode.MASKED) String confirmation) {
    usernameInput.fill(username);
    passwordInput.fill(password);
    confirmationInput.fill(confirmation);
    return this;
  }

  @Step("Отправить форму регистрации")
  public RegistrationPage registerUser() {
    submitButton.click();
    return this;
  }

  @Step("Проверить успешную регистрацию")
  public RegistrationPage shouldBeSuccessful() {
    assertThat(successMessage).isVisible();
    return this;
  }

  @Step("Вернуться к главной странице после регистрации")
  public LandingPage signIn() {
    signInLink.click();
    return new LandingPage(page);
  }
}
