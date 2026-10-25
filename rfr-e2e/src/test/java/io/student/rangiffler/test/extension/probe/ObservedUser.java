package io.student.rangiffler.test.extension.probe;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.model.TestUser;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestReporter;

public final class ObservedUser implements Runnable {
  private final TestReporter reporter;
  private final TestUser user;

  public ObservedUser(TestReporter reporter, TestUser user) {
    this.reporter = reporter;
    this.user = user;
  }

  @Override
  public void run() {
    Friendships relations = new Friendships(user.username());
    reporter.publishEntry(Map.of(
        "phase", "body",
        "username", user.username(),
        "id", user.id().toString(),
        "users", Stream.concat(Stream.of(user.username()), relations.users().stream()).collect(Collectors.joining(",")),
        "auth", Boolean.toString(new AuthAccount(user.username()).exists()),
        "profile", Boolean.toString(new Profile(user.username()).exists()),
        "friends", Long.toString(relations.friends()),
        "incoming", Long.toString(relations.incomeInvitations()),
        "thread", Thread.currentThread().getName()));
  }
}
