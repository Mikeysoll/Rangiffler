package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

public class LoginPage {
  private final Page page;

  public LoginPage(Page page) {
    this.page = page;
  }

  @Step("Войти пользователем {username}")
  public MyTravelsPage login(String username, String password) {
    page.getByPlaceholder("Type your username").fill(username);
    page.getByPlaceholder("Type your password").fill(password);
    page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in").setExact(true)).click();
    return new MyTravelsPage(page);
  }
}
