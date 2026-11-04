package io.student.rangiffler.fixture;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.model.TestUser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Владеет пользователями и связями исходящих приглашений основного пользователя. */
public final class UserWithOutcomeInvitationsFixture implements UserFixture {
  private final UserFixture origin;
  private final int count;
  private final List<UserFixture> invitees;

  private UserWithOutcomeInvitationsFixture(UserFixture origin, int count, List<UserFixture> invitees) {
    this.origin = origin;
    this.count = count;
    this.invitees = invitees;
  }

  public UserWithOutcomeInvitationsFixture(UserFixture origin, int count) {
    this(origin, count, new ArrayList<>());
    if (count < 0) {
      throw new IllegalArgumentException("Outgoing invitations count must be nonnegative but was " + count);
    }
  }

  @Override
  public void create() {
    origin.create();
    for (int index = 0; index < count; index++) {
      UserFixture invitee = new SingleUserFixture();
      invitees.add(invitee);
      invitee.create();
      new Friendship(UUID.randomUUID(), origin.user().id(), invitee.user().id(), "PENDING").create();
    }
  }

  @Override
  public TestUser user() {
    return origin.user().withOutcomeInvitations(invitees.stream().map(UserFixture::user).toList());
  }

  @Override
  public void close() {
    new TearDown().withEach(invitees.reversed()).with(origin).close();
  }
}
