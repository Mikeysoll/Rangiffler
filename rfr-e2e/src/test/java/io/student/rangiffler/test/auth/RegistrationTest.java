package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Registration;
import io.student.rangiffler.fixture.RegistrationFixture;
import io.student.rangiffler.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

@UsePlaywright
public class RegistrationTest {

  @Test
  @DisplayName("Новый пользователь может авторизоваться после регистрации")
  void shouldAuthorizeUserAfterRegistration(Page page) {
    Registration registration = new RegistrationFixture();
    try (registration) {
      new LandingPage(page)
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
