package io.student.rangiffler.test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegistrationTest {

  @Test
  @DisplayName("Новый пользователь может авторизоваться после регистрации")
  void shouldAuthorizeUserAfterRegistration() {
    String username = RandomDataUtils.randomUsername();
    String password = RandomDataUtils.randomPassword();

    try (Playwright playwright = Playwright.create();
         Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
         BrowserContext context = browser.newContext()) {
      new LandingPage(context.newPage())
          .open()
          .register()
          .fillRegistrationForm(username, password, password)
          .registerUser()
          .shouldBeSuccessful()
          .signIn()
          .login()
          .fillLoginForm(username, password)
          .signIn()
          .shouldBeAuthenticated()
          .openProfile()
          .shouldBelongTo(username);
    }
  }
}
