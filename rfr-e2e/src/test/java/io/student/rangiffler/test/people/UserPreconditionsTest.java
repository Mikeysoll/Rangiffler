package io.student.rangiffler.test.people;

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
@DisplayName("Друзья и приглашения из SQL-предусловий")
public class UserPreconditionsTest {

  @Test
  @DisplayName("Пользователь видит подготовленных друзей при наличии входящих приглашений")
  void preparedUserSeesRequestedFriends(Page page, @User(friends = 2, incomeInvitations = 3) TestUser user) {
    new LandingPage(page).open()
        .login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openFriends().checkThatUsersCount(2);
  }

  @Test
  @DisplayName("Пользователь видит подготовленные входящие приглашения при наличии друзей")
  void preparedUserSeesRequestedIncomeInvitations(Page page, @User(friends = 3, incomeInvitations = 2) TestUser user) {
    new LandingPage(page).open()
        .login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openIncomeInvitations().checkThatUsersCount(2);
  }
}
