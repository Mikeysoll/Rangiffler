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
@DisplayName("Связи пустого пользователя, созданные через UI")
public class UserConnectionsTest {

  @Test
  @DisplayName("Пустой пользователь отправляет приглашение через UI")
  void emptyUserCanSendInvitation(Page page, @User TestUser user, @User TestUser target) {
    new LandingPage(page).open()
        .login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openAllPeople().searchPeople(target.username())
        .sendInvitation(target.username()).openOutcomeInvitations().checkThatInvitationSentTo(target.username());
  }

  @Test
  @DisplayName("Пустые пользователи становятся друзьями через UI")
  void emptyUsersCanBecomeFriends(Page page, @User TestUser user, @User TestUser target) {
    new LandingPage(page).open()
        .login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openAllPeople().searchPeople(target.username()).sendInvitation(target.username())
        .logout().login().fillLoginForm(target.username(), target.password()).signIn()
        .openPeople().openIncomeInvitations().acceptInvitation(user.username())
        .openFriends().checkThatFriend(user.username());
  }
}
