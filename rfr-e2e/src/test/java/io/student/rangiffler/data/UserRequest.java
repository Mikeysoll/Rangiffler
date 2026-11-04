package io.student.rangiffler.data;

import io.student.rangiffler.annotation.User;
import io.student.rangiffler.fixture.SingleUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.fixture.UserWithFriendsFixture;
import io.student.rangiffler.fixture.UserWithIncomeInvitations;
import io.student.rangiffler.fixture.UserWithOutcomeInvitationsFixture;

public record UserRequest(int friends, int incomeInvitations, int outcomeInvitations) {
  public UserRequest {
    if (friends < 0 || incomeInvitations < 0 || outcomeInvitations < 0) {
      throw new IllegalArgumentException("User request requires nonnegative counts: friends=" + friends
          + ", incomeInvitations=" + incomeInvitations + ", outcomeInvitations=" + outcomeInvitations);
    }
  }

  public UserRequest(User user) {
    this(user.friends(), user.incomeInvitations(), user.outcomeInvitations());
  }

  public UserRequest(int friends, int incomeInvitations) {
    this(friends, incomeInvitations, 0);
  }

  public UserFixture fixture() {
    UserFixture fixture = new SingleUserFixture();
    if (friends > 0) {
      fixture = new UserWithFriendsFixture(fixture, friends);
    }
    if (incomeInvitations > 0) {
      fixture = new UserWithIncomeInvitations(fixture, incomeInvitations);
    }
    if (outcomeInvitations > 0) {
      fixture = new UserWithOutcomeInvitationsFixture(fixture, outcomeInvitations);
    }
    return fixture;
  }
}
