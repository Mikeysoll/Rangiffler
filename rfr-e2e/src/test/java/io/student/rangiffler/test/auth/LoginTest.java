package io.student.rangiffler.test.auth;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.config.EnglishContext;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.utils.RandomDataUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UsePlaywright(EnglishContext.class)
public class LoginTest {

  @Test
  @DisplayName("Пользователь, созданный в БД auth, может войти в личный кабинет")
  void shouldAuthorizeUserCreatedInAuthDatabase(Page page, @User TestUser user) {
    new LandingPage(page)
        .open()
        .login()
        .fillLoginForm(user.username(), user.password())
        .signIn()
        .shouldBeAuthenticated()
        .openProfile()
        .shouldBelongTo(user.username());
  }

  @Test
  void cannotLoginWithIncorrectPassword(Page page, @User TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password() + "_incorrect")
        .submitInvalidCredentials().checkThatBadCredentials();
  }

  @Test
  void cannotLoginWithUnknownUsername(Page page) {
    new LandingPage(page).open().login()
        .fillLoginForm(RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword())
        .submitInvalidCredentials().checkThatBadCredentials();
  }
}
