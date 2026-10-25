package io.student.rangiffler.fixture;

import io.student.rangiffler.data.AuthUserRepository;
import io.student.rangiffler.data.db.ApiProfile;
import io.student.rangiffler.data.db.RegisteredUserRows;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import java.util.UUID;

public final class SingleUserFixture implements UserFixture {
  private final AuthUserRepository repository;
  private final TestUser user;
  private boolean creationAttempted;
  private boolean closed;

  public SingleUserFixture() {
    repository = new AuthUserRepository();
    user = new TestUser(UUID.randomUUID(),
        RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword());
  }

  @Override
  public void create() {
    if (creationAttempted || closed) {
      throw new IllegalStateException("Fixture уже использована или закрыта");
    }
    creationAttempted = true;
    repository.create(user);
    new ApiProfile(user.id(), user.username()).create();
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
      new RegisteredUserRows(user.username()).run();
    }
    closed = true;
  }
}
