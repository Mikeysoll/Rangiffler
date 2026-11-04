package io.student.rangiffler.test.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.SingleUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.fixture.UserWithOutcomeInvitationsFixture;
import io.student.rangiffler.model.TestUser;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет освобождение ресурсов при прерывании подготовки исходящих приглашений. */
public class UserWithOutcomeInvitationsFixtureTest {
  @ParameterizedTest
  @ValueSource(ints = {0, 1, 2})
  void closesInviteeBeforeOriginAfterCreationFails(int completed) {
    List<String> names = new ArrayList<>();
    List<Boolean> releases = new ArrayList<>();
    boolean interrupted = false;
    try (UserFixture fixture = new UserWithOutcomeInvitationsFixture(
        new Interrupted(new SingleUserFixture(), names, releases, completed), 3)) {
      try {
        fixture.create();
      } catch (IllegalStateException exception) {
        if (!exception.getMessage().equals("Planned interruption before friendship creation")) {
          throw exception;
        }
        interrupted = true;
      }
      names.addAll(fixture.user().outcomeInvitations().stream().map(TestUser::username).toList());
    }
    assertTrue(interrupted && names.size() == completed + 2 && releases.equals(List.of(true))
        && names.stream().noneMatch(name -> new AuthAccount(name).exists()
            || new Profile(name).exists() || new Friendships(name).exists()),
        "Partial preparation left resources or released the origin before its invitee");
  }

  /** Прерывает подготовку после создания приглашённого и наблюдает порядок закрытия. */
  private static final class Interrupted implements UserFixture {
    private final UserFixture origin;
    private final List<String> names;
    private final List<Boolean> releases;
    private int remaining;

    private Interrupted(UserFixture origin, List<String> names, List<Boolean> releases, int remaining) {
      this.origin = origin;
      this.names = names;
      this.releases = releases;
      this.remaining = remaining;
    }

    @Override
    public void create() {
      origin.create();
    }

    @Override
    public TestUser user() {
      if (remaining == 0) {
        remaining = -1;
        throw new IllegalStateException("Planned interruption before friendship creation");
      }
      if (remaining > 0) {
        remaining--;
      }
      return origin.user();
    }

    @Override
    public void close() {
      releases.add(names.stream().noneMatch(name -> new Profile(name).exists() || new AuthAccount(name).exists()));
      names.add(origin.user().username());
      origin.close();
    }
  }
}
