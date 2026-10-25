package io.student.rangiffler.fixture;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.model.TestUser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class UserWithFriendsFixture implements UserFixture {
  private final UserFixture origin;
  private final int count;
  private final List<UserFixture> friends;

  private UserWithFriendsFixture(UserFixture origin, int count, List<UserFixture> friends) {
    this.origin = origin;
    this.count = count;
    this.friends = friends;
  }

  public UserWithFriendsFixture(UserFixture origin, int count) {
    this(origin, count, new ArrayList<>());
    if (count < 0) {
      throw new IllegalArgumentException("Friends count must be nonnegative but was " + count);
    }
  }

  @Override
  public void create() {
    origin.create();
    for (int index = 0; index < count; index++) {
      UserFixture friend = new SingleUserFixture();
      friends.add(friend);
      friend.create();
      new Friendship(UUID.randomUUID(), user().id(), friend.user().id(), "ACCEPTED").create();
    }
  }

  @Override
  public TestUser user() {
    return origin.user();
  }

  @Override
  public void close() {
    new TearDown().withEach(friends.reversed()).with(origin).close();
  }
}
