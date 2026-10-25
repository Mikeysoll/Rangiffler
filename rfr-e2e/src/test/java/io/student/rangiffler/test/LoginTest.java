package io.student.rangiffler.test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoginTest {

  @Test
  @DisplayName("Пользователь, созданный в БД auth, может войти в личный кабинет")
  void shouldAuthorizeUserCreatedInAuthDatabase(@User TestUser user) {
    try (Playwright playwright = Playwright.create();
         Browser browser = playwright.chromium().launch();
         BrowserContext context = browser.newContext()) {
      new LandingPage(context.newPage())
          .open()
          .login()
          .fillLoginForm(user.username(), user.password())
          .signIn()
          .shouldBeAuthenticated()
          .openProfile()
          .shouldBelongTo(user.username());
    }
  }
}
