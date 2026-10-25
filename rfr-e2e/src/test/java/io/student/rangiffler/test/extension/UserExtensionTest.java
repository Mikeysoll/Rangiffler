package io.student.rangiffler.test.extension;

import io.student.rangiffler.annotation.ProbeTarget;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.extension.UserExtension;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.test.extension.probe.ObservedUser;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
  void preparesAccountAndProfileBeforeTestBody(TestReporter reporter) {
    EngineExecutionResults result = execute(Observed.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && result.testEvents().failed().count() == 0
        && body.get("auth").equals("true") && body.get("profile").equals("true"),
        "UserExtension did not prepare both account and profile before the test body");
  }

  @Test
  void reportsLifecycleInExecutionOrder(TestReporter reporter) {
    EngineExecutionResults result = execute(Observed.class, reporter);
    assertEquals(List.of("before", "body", "after"), entries(result).stream().map(entry -> entry.get("phase")).toList(),
        "Lifecycle reports did not follow before, body and after order");
  }

  @Test
  void removesUserAfterSuccessfulExecution(TestReporter reporter) {
    EngineExecutionResults result = execute(Observed.class, reporter);
    assertTrue(result.testEvents().succeeded().count() == 1 && removed(body(result).get("users"), reporter),
        "Successful execution left user data after Store closure");
  }

  @Test
  void removesWholeFixtureAfterTestBodyFails(TestReporter reporter) {
    EngineExecutionResults result = execute(Failing.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && failure(result).contains("Planned probe failure")
        && removed(body(result).get("users"), reporter),
        "Failed test left its primary user, friends or inviters after Store closure");
  }

  @Test
  void preparesRequestedFriends(TestReporter reporter) {
    EngineExecutionResults result = execute(WithFriends.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("friends").equals("3")
        && body.get("incoming").equals("0") && removed(body.get("users"), reporter),
        "Friends parameter was not applied or its fixture was not cleaned");
  }

  @Test
  void preparesRequestedIncomingInvitations(TestReporter reporter) {
    EngineExecutionResults result = execute(WithInvitations.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("friends").equals("0")
        && body.get("incoming").equals("2") && removed(body.get("users"), reporter),
        "Incoming invitations parameter was not applied in the correct direction or cleaned");
  }

  @Test
  void combinesBothDecoratorParameters(TestReporter reporter) {
    EngineExecutionResults result = execute(WithRelations.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("friends").equals("2")
        && body.get("incoming").equals("3") && removed(body.get("users"), reporter),
        "Both decorator parameters were not combined or the complete fixture was not cleaned");
  }

  @Test
  void assignsDistinctUsersToTwoParameters(TestReporter reporter) {
    EngineExecutionResults result = execute(TwoUsers.class, reporter);
    Map<String, String> body = body(result);
    assertTrue(result.testEvents().succeeded().count() == 1 && body.get("distinct").equals("true")
        && body.get("present").equals("true") && removed(body.get("users"), reporter),
        "Two User parameters shared an account or were not independently cleaned");
  }

  @Test
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
  void doesNotInterfereWithTestReporterWithoutUser(TestReporter reporter) {
    EngineExecutionResults result = execute(WithoutUser.class, reporter);
    assertTrue(result.testEvents().succeeded().count() == 1 && entries(result).equals(List.of(Map.of("phase", "without-user"))),
        "UserExtension interfered with a test using only TestReporter");
  }

  @Test
  void cannotResolveAnnotatedParameterOfWrongType(TestReporter reporter) {
    EngineExecutionResults result = execute(WrongType.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && failure(result).contains("@User поддерживает только параметры типа TestUser"),
        "Wrong annotated parameter type was not rejected before the body");
  }

  @Test
  void cannotResolveNegativeFriendsCount(TestReporter reporter) {
    EngineExecutionResults result = execute(NegativeFriends.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && failure(result).contains("friends=-7"), "Negative friends count was not rejected before the body");
  }

  @Test
  void cannotResolveNegativeIncomingInvitationsCount(TestReporter reporter) {
    EngineExecutionResults result = execute(NegativeInvitations.class, reporter);
    assertTrue(result.testEvents().failed().count() == 1 && entries(result).isEmpty()
        && failure(result).contains("incomeInvitations=-11"), "Negative incoming invitations count was not rejected before the body");
  }

  @Test
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
  public static class Observed {
    @BeforeEach
    void before(TestReporter reporter) {
      reporter.publishEntry("phase", "before");
    }
    @Test
    void observed(TestReporter reporter, @User TestUser user) {
      new ObservedUser(reporter, user).run();
    }
    @AfterEach
    void after(TestReporter reporter) {
      reporter.publishEntry("phase", "after");
    }
  }

  @ProbeTarget
  public static class Failing {
    @Test
    void failing(TestReporter reporter, @User(friends = 2, incomeInvitations = 3) TestUser user) {
      new ObservedUser(reporter, user).run();
      throw new AssertionError("Planned probe failure");
    }
  }

  @ProbeTarget
  public static class WithFriends {
    @Test
    void observed(TestReporter reporter, @User(friends = 3) TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  public static class WithInvitations {
    @Test
    void observed(TestReporter reporter, @User(incomeInvitations = 2) TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  public static class WithRelations {
    @Test
    void observed(TestReporter reporter, @User(friends = 2, incomeInvitations = 3) TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  public static class TwoUsers {
    @Test
    void observed(TestReporter reporter, @User TestUser first, @User TestUser second) {
      reporter.publishEntry(Map.of("phase", "body", "users", first.username() + "," + second.username(),
          "distinct", Boolean.toString(!first.id().equals(second.id()) && !first.username().equals(second.username())),
          "present", Boolean.toString(new AuthAccount(first.username()).exists() && new Profile(first.username()).exists()
              && new AuthAccount(second.username()).exists() && new Profile(second.username()).exists())));
    }
  }

  @ProbeTarget
  public static class Concurrent {
    @Test
    void first(TestReporter reporter, @User TestUser user) {
      new ObservedUser(reporter, user).run();
    }
    @Test
    void second(TestReporter reporter, @User TestUser user) {
      new ObservedUser(reporter, user).run();
    }
  }

  @ProbeTarget
  @ExtendWith(UserExtension.class)
  public static class WithoutUser {
    @Test
    void observed(TestReporter reporter) {
      reporter.publishEntry("phase", "without-user");
    }
  }

  @ProbeTarget
  public static class WrongType {
    @Test
    void observed(TestReporter reporter, @User String user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }

  @ProbeTarget
  public static class NegativeFriends {
    @Test
    void observed(TestReporter reporter, @User(friends = -7) TestUser user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }

  @ProbeTarget
  public static class NegativeInvitations {
    @Test
    void observed(TestReporter reporter, @User(incomeInvitations = -11) TestUser user) {
      reporter.publishEntry("phase", "unexpected-body");
    }
  }
}
