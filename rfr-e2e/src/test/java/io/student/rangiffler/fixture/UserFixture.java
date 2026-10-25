package io.student.rangiffler.fixture;

import io.student.rangiffler.data.AuthUserRepository;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.utils.RandomDataUtils;
import java.util.UUID;

public final class UserFixture implements AutoCloseable {
  private final AuthUserRepository repository;
  private final TestUser user;
  private boolean creationAttempted;
  private boolean closed;

  public UserFixture() {
    repository = new AuthUserRepository();
    user = new TestUser(UUID.randomUUID(),
        RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword());
  }

  public void create() {
    if (creationAttempted || closed) {
      throw new IllegalStateException("Fixture уже использована или закрыта");
    }
    creationAttempted = true;
    repository.create(user);
  }

  public TestUser user() {
    return user;
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }
    if (creationAttempted) {
      repository.delete(user);
    }
    closed = true;
  }
}
