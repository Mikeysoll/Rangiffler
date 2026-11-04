package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Проверяет состав и доступные действия вкладки outcomeInvitations. */
@Tag("ui")
@UsePlaywright
@DisplayName("OutcomeInvitationsTabTest")
public class OutcomeInvitationsTabTest {
  @Test
  void showsRequestedUser(Page page, @User(outcomeInvitations = 1) TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openOutcomeInvitations().checkThatUsers(user.outcomeInvitations().getFirst().username());
  }

  @Test
  void cannotShowOtherRelationTypes(Page page,
      @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openOutcomeInvitations().checkThatUsers(user.outcomeInvitations().getFirst().username());
  }

  @Test
  void showsActionsInRelatedUserRow(Page page, @User(outcomeInvitations = 1) TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openOutcomeInvitations().checkThatRowButtons(user.outcomeInvitations().getFirst().username(), "Cancel")
        .checkThatWaitingFor(user.outcomeInvitations().getFirst().username());
  }

  @Test
  void showsEmptyStateWithoutRelations(Page page, @User TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openOutcomeInvitations().checkThatEmpty();
  }
}