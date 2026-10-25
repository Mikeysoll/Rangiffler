package io.student.rangiffler.fixture;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.model.TestUser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UserWithIncomeInvitations implements UserFixture {
  private final UserFixture origin;
  private final int count;
  private final List<UserFixture> inviters;

  private UserWithIncomeInvitations(UserFixture origin, int count, List<UserFixture> inviters) {
    this.origin = origin;
    this.count = count;
    this.inviters = inviters;
  }

  public UserWithIncomeInvitations(UserFixture origin, int count) {
    this(origin, count, new ArrayList<>());
    if (count < 0) {
      throw new IllegalArgumentException("Incoming invitations count must be nonnegative but was " + count);
    }
  }

  @Override
  public void create() {
    origin.create();
    for (int index = 0; index < count; index++) {
      UserFixture inviter = new SingleUserFixture();
      inviters.add(inviter);
      inviter.create();
      new Friendship(UUID.randomUUID(), inviter.user().id(), user().id(), "PENDING").create();
    }
  }

  @Override
  public TestUser user() {
    return origin.user();
  }

  @Override
  public void close() {
    new TearDown().withEach(inviters.reversed()).with(origin).close();
  }
}
