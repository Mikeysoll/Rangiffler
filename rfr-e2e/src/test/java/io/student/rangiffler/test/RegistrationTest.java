package io.student.rangiffler.test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Registration;
import io.student.rangiffler.fixture.RegistrationFixture;
import io.student.rangiffler.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RegistrationTest {

  @Test
  @DisplayName("Новый пользователь может авторизоваться после регистрации")
  void shouldAuthorizeUserAfterRegistration() {
    Registration registration = new RegistrationFixture();
    try (registration;
         Playwright playwright = Playwright.create();
         Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
         BrowserContext context = browser.newContext()) {
      new LandingPage(context.newPage())
          .open()
          .register()
          .fillRegistrationForm(registration.username(), registration.password(), registration.password())
          .registerUser()
          .shouldBeSuccessful()
          .signIn()
          .login()
          .fillLoginForm(registration.username(), registration.password())
          .signIn()
          .shouldBeAuthenticated()
          .openProfile()
          .shouldBelongTo(registration.username());
    }
    assertFalse(new AuthAccount(registration.username()).exists() || new Profile(registration.username()).exists(),
        "Registered user was not removed from auth and API");
  }
}
