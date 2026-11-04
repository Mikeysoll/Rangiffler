package io.student.rangiffler.test.data.db;

import io.student.rangiffler.data.UserRequest;
import io.student.rangiffler.data.db.UserSnapshot;
import io.student.rangiffler.fixture.UserFixture;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет, что снимок замечает создание и освобождение тестовых данных. */
public class UserSnapshotTest {
  @Test
  void detectsUserCreationAndRestoresSnapshotAfterClosure() {
    UserSnapshot snapshot;
    List<String> names;
    boolean changed;
    try (UserFixture fixture = new UserRequest(0, 0, 1).fixture()) {
      names = List.of(fixture.user().username());
      snapshot = new UserSnapshot(names);
      fixture.create();
      changed = !snapshot.equals(new UserSnapshot(names));
    }
    assertTrue(changed && snapshot.equals(new UserSnapshot(names)),
        "Snapshot missed fixture creation or found data remaining after closure");
  }

  @Test
  void preservesBinaryIdentitiesAndIgnoresUnrelatedUsers() {
    boolean stable;
    try (UserFixture fixture = new UserRequest(0, 0, 1).fixture();
        UserFixture unrelated = new UserRequest(0, 0).fixture()) {
      fixture.create();
      List<String> names = List.of(fixture.user().username());
      UserSnapshot snapshot = new UserSnapshot(names);
      unrelated.create();
      stable = snapshot.equals(new UserSnapshot(names)) && snapshot.accounts().size() == 1
          && snapshot.profiles().size() == 1 && snapshot.relations().size() == 1
          && java.util.stream.Stream.of(snapshot.accounts(), snapshot.profiles(), snapshot.relations())
              .flatMap(List::stream).allMatch(id -> id.matches("[0-9A-F]{32}"));
    }
    assertTrue(stable, "Snapshot lost binary identity or included unrelated users");
  }
}
