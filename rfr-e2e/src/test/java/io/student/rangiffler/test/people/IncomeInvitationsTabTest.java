package io.student.rangiffler.test.people;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Проверяет состав и доступные действия вкладки incomeInvitations. */
@Tag("ui")
@UsePlaywright
@DisplayName("IncomeInvitationsTabTest")
public class IncomeInvitationsTabTest {
  @Test
  void showsRequestedUser(Page page, @User(incomeInvitations = 1) TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openIncomeInvitations().checkThatUsers(user.incomeInvitations().getFirst().username());
  }

  @Test
  void cannotShowOtherRelationTypes(Page page,
      @User(friends = 1, incomeInvitations = 1, outcomeInvitations = 1) TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openIncomeInvitations().checkThatUsers(user.incomeInvitations().getFirst().username());
  }

  @Test
  void showsActionsInRelatedUserRow(Page page, @User(incomeInvitations = 1) TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openIncomeInvitations().checkThatRowButtons(user.incomeInvitations().getFirst().username(), "Accept", "Decline");
  }

  @Test
  void showsEmptyStateWithoutRelations(Page page, @User TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openPeople().openIncomeInvitations().checkThatEmpty();
  }
}