package io.student.rangiffler.test.extension;

import io.student.rangiffler.annotation.ProbeTarget;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.data.db.UserSnapshot;
import io.student.rangiffler.extension.UserExtension;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.test.extension.probe.ObservedUser;
import io.student.rangiffler.test.extension.probe.UserCreation;
import org.aspectj.lang.Aspects;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@DisplayName("Контракт UserExtension через EngineTestKit")
public class UserExtensionTest {

  @Test
  @DisplayName("UserExtension создаёт учётную запись и профиль до выполнения теста")
  void preparesAccountAndProfileBeforeTestBody(TestReporter reporter) {
    EngineExecutionResults result = execute(Observed.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && result.testEvents().failed().count() == 0
        && body.get("auth").equals("true") && body.get("profile").equals("true"),
        "UserExtension did not prepare both account and profile before the test body");
  }

  @Test
  @DisplayName("Этапы жизненного цикла выполняются в порядке до теста, тело теста, после теста")
  void reportsLifecycleInExecutionOrder(TestReporter reporter) {
    EngineExecutionResults result = execute(Observed.class, reporter);
    assertEquals(List.of("before", "body", "after"), entries(result).stream().map(entry -> entry.get("phase")).toList(),
        "Lifecycle reports did not follow before, body and after order");
  }

  @Test
  @DisplayName("UserExtension удаляет пользователя после успешного теста")
  void removesUserAfterSuccessfulExecution(TestReporter reporter) {
    EngineExecutionResults result = execute(Observed.class, reporter);
    assertTrue(result.testEvents().succeeded().count() == 1 && removed(body(result).get("users"), reporter),
        "Successful execution left user data after Store closure");
  }

  @Test
  @DisplayName("UserExtension удаляет пользователя и его связи после ошибки в теле теста")
  void removesWholeFixtureAfterTestBodyFails(TestReporter reporter) {
    EngineExecutionResults result = execute(Failing.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && failure(result).contains("Planned probe failure")
        && removed(body(result).get("users"), reporter),
        "Failed test left its primary user, friends or inviters after Store closure");
  }

  @Test
  @DisplayName("UserExtension создаёт запрошенных друзей и удаляет фикстуру после теста")
  void preparesRequestedFriends(TestReporter reporter) {
    EngineExecutionResults result = execute(WithFriends.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("friends").equals("3")
        && body.get("incoming").equals("0") && removed(body.get("users"), reporter),
        "Friends parameter was not applied or its fixture was not cleaned");
  }

  @Test
  @DisplayName("UserExtension создаёт запрошенные входящие приглашения и удаляет фикстуру после теста")
  void preparesRequestedIncomingInvitations(TestReporter reporter) {
    EngineExecutionResults result = execute(WithInvitations.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("friends").equals("0")
        && body.get("incoming").equals("2") && removed(body.get("users"), reporter),
        "Incoming invitations parameter was not applied in the correct direction or cleaned");
  }

  @Test
  @DisplayName("UserExtension совместно применяет параметры друзей и приглашений и удаляет фикстуру")
  void combinesBothDecoratorParameters(TestReporter reporter) {
    EngineExecutionResults result = execute(WithRelations.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("friends").equals("2")
        && body.get("incoming").equals("3") && removed(body.get("users"), reporter),
        "Both decorator parameters were not combined or the complete fixture was not cleaned");
  }

  @Test
  @DisplayName("Два параметра User получают разных пользователей и очищаются независимо")
  void assignsDistinctUsersToTwoParameters(TestReporter reporter) {
    EngineExecutionResults result = execute(TwoUsers.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("distinct").equals("true")
        && body.get("present").equals("true") && removed(body.get("users"), reporter),
        "Two User parameters shared an account or were not independently cleaned");
  }

  @Test
  @DisplayName("Параллельные тестовые методы получают отдельных пользователей и удаляют свои данные")
  void isolatesConcurrentTestMethodStores(TestReporter reporter) {
    EngineExecutionResults result = EngineTestKit.engine("junit-jupiter").selectors(selectClass(Concurrent.class))
        .configurationParameter("rfr.probe.enabled", "true")
        .configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
        .configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent")
        .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "fixed")
        .configurationParameter("junit.jupiter.execution.parallel.config.fixed.parallelism", "2")
        .configurationParameter("junit.jupiter.execution.parallel.config.fixed.max-pool-size", "2").execute();
    entries(result).forEach(reporter::publishEntry);
    List<Map<String, String>> bodies = entries(result);
    assertTrue(result.testEvents().succeeded().count() == 2 && bodies.size() == 2
        && bodies.stream().map(entry -> entry.get("id")).distinct().count() == 2
        && bodies.stream().allMatch(entry -> entry.get("auth").equals("true") && entry.get("profile").equals("true"))
        && bodies.stream().allMatch(entry -> removed(entry.get("users"), reporter)),
        "Concurrent test methods reused a Store resource or left database records");
  }

  @Test
  @DisplayName("UserExtension не мешает тесту с TestReporter без параметра User")
  void doesNotInterfereWithTestReporterWithoutUser(TestReporter reporter) {
    EngineExecutionResults result = execute(WithoutUser.class, reporter);
    assertTrue(result.testEvents().succeeded().count() == 1 && entries(result).equals(List.of(Map.of("phase", "without-user"))),
        "UserExtension interfered with a test using only TestReporter");
  }

  @Test
  @DisplayName("UserExtension отклоняет параметр User неверного типа до выполнения тела теста")
  void cannotResolveAnnotatedParameterOfWrongType(TestReporter reporter) {
    EngineExecutionResults result = execute(WrongType.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && failure(result).contains("@User поддерживает только параметры типа TestUser"),
        "Wrong annotated parameter type was not rejected before the body");
  }

  @Test
  @DisplayName("UserExtension отклоняет отрицательное количество друзей до выполнения тела теста")
  void cannotResolveNegativeFriendsCount(TestReporter reporter) {
    EngineExecutionResults result = execute(NegativeFriends.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && failure(result).contains("friends=-7"), "Negative friends count was not rejected before the body");
  }

  @Test
  @DisplayName("UserExtension отклоняет отрицательное количество приглашений до выполнения тела теста")
  void cannotResolveNegativeIncomingInvitationsCount(TestReporter reporter) {
    EngineExecutionResults result = execute(NegativeInvitations.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && failure(result).contains("incomeInvitations=-11"), "Negative incoming invitations count was not rejected before the body");
  }

  @Test
  @DisplayName("Вспомогательный сценарий не запускается без явного разрешения EngineTestKit")
  void cannotRunProbeWithoutExplicitOptIn() {
    EngineExecutionResults result = EngineTestKit.engine("junit-jupiter").selectors(selectClass(Observed.class))
        .configurationParameter("rfr.probe.enabled", "false").execute();
    assertTrue(result.testEvents().started().count() == 0 && result.containerEvents().skipped().count() == 1,
        "Probe ran outside an explicitly enabled EngineTestKit execution");
  }

  private EngineExecutionResults execute(Class<?> target, TestReporter reporter) {
    EngineExecutionResults result = EngineTestKit.engine("junit-jupiter").selectors(selectClass(target))
        .configurationParameter("rfr.probe.enabled", "true")
        .configurationParameter("junit.jupiter.execution.parallel.enabled", "false").execute();
    entries(result).forEach(reporter::publishEntry);
    return result;
  }

  @Test
  @DisplayName("Исходящее приглашение имеет правильное направление и очищается вместе с пользователями")
  void preparesAndRemovesOutgoingInvitation(TestReporter reporter) {
    EngineExecutionResults result = execute(WithOutcomeInvitation.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("valid").equals("true")
        && removed(body.get("users"), reporter),
        "Outgoing invitation had incorrect participants or status, or left database records");
  }

  @Test
  @DisplayName("Отрицательное количество исходящих приглашений отклоняется до подготовки фикстуры")
  void cannotResolveNegativeOutgoingInvitationsCount(TestReporter reporter) {
    UserCreation observation = Aspects.aspectOf(UserCreation.class);
    EngineExecutionResults calibration = execute(Observed.class, reporter);
    List<String> names = observation.usernames();
    UserSnapshot snapshot = new UserSnapshot(names);
    EngineExecutionResults result = execute(NegativeOutcomeInvitations.class, reporter);
    Throwable cause = result.testEvents().failed().stream()
        .map(event -> event.getRequiredPayload(TestExecutionResult.class))
        .flatMap(event -> event.getThrowable().stream())
        .flatMap(error -> Optional.ofNullable(error.getCause()).stream()).findFirst().orElseThrow();
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && cause instanceof IllegalArgumentException && cause.getMessage().contains("outcomeInvitations=-1")
        && calibration.testEvents().succeeded().count() == 1 && names.size() == 1
        && observation.usernames().equals(names) && snapshot.equals(new UserSnapshot(names))
        && removed(String.join(",", names), reporter),
        "Negative outgoing invitations count invoked create, had an incorrect cause or left database records");
  }

  @Test
  @DisplayName("Мишени исходящих приглашений отключены без явного разрешения EngineTestKit")
  void cannotRunOutgoingProbesWithoutExplicitOptIn() {
    EngineExecutionResults result = EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(WithOutcomeInvitation.class), selectClass(NegativeOutcomeInvitations.class))
        .configurationParameter("rfr.probe.enabled", "false").execute();
    assertTrue(result.testEvents().started().count() == 0 && result.containerEvents().skipped().count() == 2,
        "Outgoing invitation probes ran without explicit EngineTestKit opt-in");
  }

  private List<Map<String, String>> entries(EngineExecutionResults result) {
    return result.testEvents().reportingEntryPublished().stream()
        .map(event -> event.getRequiredPayload(ReportEntry.class).getKeyValuePairs()).toList();
  }

  private Map<String, String> body(EngineExecutionResults result) {
    return entries(result).stream().filter(entry -> entry.getOrDefault("phase", "").equals("body"))
        .findFirst().orElseThrow(() -> new AssertionError("Probe did not report its body state"));
  }

  private boolean removed(String users, TestReporter reporter) {
    List<String> names = Arrays.asList(users.split(","));
    boolean removed = names.stream().noneMatch(name -> new AuthAccount(name).exists()
        || new Profile(name).exists() || new Friendships(name).exists());
    reporter.publishEntry(Map.of("phase", "store-closed", "users", users, "removed", Boolean.toString(removed)));
    return removed;
  }

  private String failure(EngineExecutionResults result) {
    return result.testEvents().failed().stream().map(event -> event.getRequiredPayload(TestExecutionResult.class)
        .getThrowable().map(Throwable::toString).orElse("Missing failure")).findFirst().orElse("Missing failure");
  }

  @ProbeTarget
  @DisplayName("Подготовка пользователя и этапы жизненного цикла")
  public static class Observed {
    @BeforeEach
    void before(TestReporter reporter) {
      reporter.publishEntry("phase", "before");
    }
    @Test
    @DisplayName("Вспомогательный сценарий сообщает о подготовленной учётной записи и профиле")
    void observed(TestReporter reporter, @User TestUser user) {
      new ObservedUser(reporter, user).run();
    }
    @AfterEach
    void after(TestReporter reporter) {
      reporter.publishEntry("phase", "after");
    }
  }

  @ProbeTarget
  @DisplayName("Очистка фикстуры после намеренной ошибки")
  public static class Failing {
    @Test
    @DisplayName("Вспомогательный сценарий намеренно завершается с ошибкой после подготовки пользователя и связей")
    void failing(TestReporter reporter, @User(friends = 2, incomeInvitations = 3, outcomeInvitations = 1) TestUser user) {
      new ObservedUser(reporter, user).run();
      throw new AssertionError("Planned probe failure");
    }
  }

  @ProbeTarget
  @DisplayName("Подготовка пользователя с друзьями")
  public static class WithFriends {
    @Test
    @DisplayName("Вспомогательный сценарий сообщает о пользователе с тремя друзьями")
    void observed(TestReporter reporter, @User(friends = 3) TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  @DisplayName("Подготовка пользователя с входящими приглашениями")
  public static class WithInvitations {
    @Test
    @DisplayName("Вспомогательный сценарий сообщает о пользователе с двумя входящими приглашениями")
    void observed(TestReporter reporter, @User(incomeInvitations = 2) TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  @DisplayName("Совместная подготовка друзей и приглашений")
  public static class WithRelations {
    @Test
    @DisplayName("Вспомогательный сценарий сообщает о пользователе с двумя друзьями и тремя приглашениями")
    void observed(TestReporter reporter, @User(friends = 2, incomeInvitations = 3) TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  @DisplayName("Независимая подготовка двух пользователей")
  public static class TwoUsers {
    @Test
    @DisplayName("Вспомогательный сценарий сообщает о двух разных подготовленных пользователях")
    void observed(TestReporter reporter, @User TestUser first, @User TestUser second) {
      reporter.publishEntry(Map.of("phase", "body", "users", first.username() + "," + second.username(),
          "distinct", Boolean.toString(!first.id().equals(second.id()) && !first.username().equals(second.username())),
          "present", Boolean.toString(new AuthAccount(first.username()).exists() && new Profile(first.username()).exists()
              && new AuthAccount(second.username()).exists() && new Profile(second.username()).exists())));
    }
  }

  @ProbeTarget
  @DisplayName("Изоляция пользователей параллельных тестов")
  public static class Concurrent {
    @Test
    @DisplayName("Первый параллельный сценарий сообщает о своём подготовленном пользователе")
    void first(TestReporter reporter, @User TestUser user) {
      new ObservedUser(reporter, user).run();
    }
    @Test
    @DisplayName("Второй параллельный сценарий сообщает о своём подготовленном пользователе")
    void second(TestReporter reporter, @User TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  @ExtendWith(UserExtension.class)
  @DisplayName("Работа расширения без параметра User")
  public static class WithoutUser {
    @Test
    @DisplayName("Вспомогательный сценарий с TestReporter выполняется без параметра User")
    void observed(TestReporter reporter) {
      reporter.publishEntry("phase", "without-user");
    }
  }

  @ProbeTarget
  @DisplayName("Отклонение параметра User неверного типа")
  public static class WrongType {
    @Test
    @DisplayName("Вспомогательный сценарий запрашивает строковый параметр с аннотацией User")
    void observed(TestReporter reporter, @User String user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }

  @ProbeTarget
  @DisplayName("Отклонение отрицательного количества друзей")
  public static class NegativeFriends {
    @Test
    @DisplayName("Вспомогательный сценарий запрашивает отрицательное количество друзей")
    void observed(TestReporter reporter, @User(friends = -7) TestUser user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }

  @ProbeTarget
  @DisplayName("Отклонение отрицательного количества приглашений")
  public static class NegativeInvitations {
    @Test
    @DisplayName("Вспомогательный сценарий запрашивает отрицательное количество входящих приглашений")
    void observed(TestReporter reporter, @User(incomeInvitations = -11) TestUser user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }

  @ProbeTarget
  @DisplayName("Создание исходящего приглашения")
  public static class WithOutcomeInvitation {
    @Test
    void observed(TestReporter reporter, @User(outcomeInvitations = 1) TestUser user) {
      TestUser invitee = user.outcomeInvitations().getFirst();
      reporter.publishEntry(Map.of("phase", "body", "users", user.username() + "," + invitee.username(),
          "valid", Boolean.toString(user.outcomeInvitations().size() == 1
              && new AuthAccount(user.username()).exists() && new Profile(user.username()).exists()
              && new AuthAccount(invitee.username()).exists() && new Profile(invitee.username()).exists()
              && new Friendships(user.username()).exists() && new Friendships(invitee.username()).exists()
              && new Friendships(invitee.username()).incomeInvitations() == 1
              && new Friendships(invitee.username()).users().equals(List.of(user.username()))
              && new Friendships(user.username()).incomeInvitations() == 0)));
    }
  }

  @ProbeTarget
  @DisplayName("Отклонение отрицательного количества исходящих приглашений")
  public static class NegativeOutcomeInvitations {
    @Test
    void observed(TestReporter reporter, @User(outcomeInvitations = -1) TestUser user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }
}
