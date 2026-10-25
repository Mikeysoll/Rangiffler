package io.student.rangiffler.data;

import io.student.rangiffler.annotation.User;
import io.student.rangiffler.fixture.SingleUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.fixture.UserWithFriendsFixture;
import io.student.rangiffler.fixture.UserWithIncomeInvitations;

public record UserRequest(int friends, int incomeInvitations) {
  public UserRequest {
    if (friends < 0 || incomeInvitations < 0) {
      throw new IllegalArgumentException("User request requires nonnegative counts: friends=" + friends
          + ", incomeInvitations=" + incomeInvitations);
    }
  }

  public UserRequest(User user) {
    this(user.friends(), user.incomeInvitations());
  }

  public UserFixture fixture() {
    UserFixture fixture = new SingleUserFixture();
    if (friends > 0) {
      fixture = new UserWithFriendsFixture(fixture, friends);
    }
    if (incomeInvitations > 0) {
      fixture = new UserWithIncomeInvitations(fixture, incomeInvitations);
    }
    return fixture;
  }
}
