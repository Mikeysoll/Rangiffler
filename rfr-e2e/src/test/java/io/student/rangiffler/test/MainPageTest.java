package io.student.rangiffler.test;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
@UsePlaywright
@DisplayName("Главная страница с SQL-предусловием")
public class MainPageTest {

  @Test
  @DisplayName("Подготовленный пользователь входит через UI и видит главную")
  void preparedUserCanLogin(Page page, @User TestUser user) {
    new LandingPage(page).open()
        .login()
        .fillLoginForm(user.username(), user.password())
        .signIn()
        .checkThatMainElementsVisible()
        .openProfile()
        .shouldBelongTo(user.username());
  }
}
