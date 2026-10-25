package io.student.rangiffler.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.utils.RandomDataUtils;

public final class RegistrationFixture implements Registration {
  private final String username;
  private final String password;
  public RegistrationFixture(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public RegistrationFixture() {
    this(RandomDataUtils.randomUsername(), RandomDataUtils.randomPassword());
  }

  @Override
  public String username() {
    return username;
  }

  @Override
  public String password() {
    return password;
  }

  @Override
  public void close() {
    new TearDown().with(new Profile(username)::delete).with(new AuthAccount(username)::delete).close();
  }
}
