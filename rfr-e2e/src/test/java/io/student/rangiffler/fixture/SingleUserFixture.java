package io.student.rangiffler.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import java.util.UUID;

public final class SingleUserFixture implements UserFixture {
  private final TestUser user;
  private boolean creationAttempted;
  private boolean closed;

  public SingleUserFixture() {
    user = new TestUser(UUID.randomUUID(),
        RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword());
  }

  @Override
  public void create() {
    if (creationAttempted || closed) {
      throw new IllegalStateException("Fixture уже использована или закрыта");
    }
    creationAttempted = true;
    new AuthAccount(user.id(), user.username(), user.password()).create();
    new Profile(user.id(), user.username()).create();
  }

  @Override
  public TestUser user() {
    return user;
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    if (creationAttempted) {
      new TearDown().with(new Profile(user.username())::delete).with(new AuthAccount(user.username())::delete).close();
    }
    closed = true;
  }
}
