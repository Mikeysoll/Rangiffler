package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class RegistrationPage {
  private final Page page;

  public RegistrationPage(Page page) {
    this.page = page;
  }

  @Step("Зарегистрировать пользователя {username}")
  public RegistrationPage register(String username, String password) {
    page.getByPlaceholder("Type your username").fill(username);
    page.getByPlaceholder("Type your password", new Page.GetByPlaceholderOptions().setExact(true)).fill(password);
    page.getByPlaceholder("Repeat your password").fill(password);
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign up").setExact(true)).click();
    return this;
  }

  @Step("Проверить успешную регистрацию")
  public RegistrationPage shouldBeSuccessful() {
    assertThat(page.getByText("Congratulations! You've registered!", new Page.GetByTextOptions().setExact(true))).isVisible();
    return this;
  }

  @Step("Вернуться к главной странице после регистрации")
  public LandingPage signIn() {
    page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign in").setExact(true)).click();
    return new LandingPage(page);
  }
}
