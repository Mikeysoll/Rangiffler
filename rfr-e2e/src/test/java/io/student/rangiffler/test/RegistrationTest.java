package io.student.rangiffler.test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.page.LandingPage;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RegistrationTest {

  @Test
  @DisplayName("Новый пользователь может авторизоваться после регистрации")
  void shouldAuthorizeUserAfterRegistration() {
    String username = "ui_" + UUID.randomUUID().toString().replace("-", "");
    String password = UUID.randomUUID().toString().substring(0, 12);
    String baseUrl = System.getenv().getOrDefault("RFR_BASE_URL", "http://localhost:3001");

    try (Playwright playwright = Playwright.create();
         Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
         BrowserContext context = browser.newContext()) {
      new LandingPage(context.newPage())
          .open(baseUrl)
          .register()
          .register(username, password)
          .shouldBeSuccessful()
          .signIn()
          .login()
          .login(username, password)
          .shouldBeOpened()
          .openProfile()
          .shouldBelongTo(username);
    }
  }
}
