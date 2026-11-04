package io.student.rangiffler.test.extension;

import io.student.rangiffler.annotation.ProbeTarget;
import io.student.rangiffler.annotation.StaticUser;
import io.student.rangiffler.fixture.StaticUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.model.TestUser;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/** Проверяет возврат пользователей пула при закрытии Store через EngineTestKit. */
@Isolated("Owns all three static users during pool assertions")
@Execution(ExecutionMode.SAME_THREAD)
public class UsersQueueExtensionTest {
  @Test
  void fourSequentialTestsReuseThreeUsers() {
    EngineExecutionResults result = execute(Sequential.class);
    List<String> users = users(result);
    assertTrue(result.testEvents().succeeded().count() == 4 && result.testEvents().failed().count() == 0
        && users.size() == 4 && users.stream().distinct().count() == 3
        && users.getFirst().equals(users.getLast()) && complete(),
        "Four sequential tests exhausted the pool or failed to return three unique users");
  }

  @Test
  void failedTestReturnsUserBeforeNextTest() {
    EngineExecutionResults result = execute(Failing.class);
    List<String> users = users(result);
    assertTrue(result.testEvents().failed().count() == 1 && result.testEvents().succeeded().count() == 1
        && result.testEvents().failed().stream().allMatch(event -> event
            .getRequiredPayload(org.junit.platform.engine.TestExecutionResult.class).getThrowable()
            .map(error -> error.getMessage().equals("Planned queue probe failure")).orElse(false))
        && users.size() == 2 && List.of(users.getLast().split(",")).contains(users.getFirst()) && complete(),
        "Failed test did not return its user before the next test leased the complete pool");
  }

  @Test
  void cannotRunQueueProbesWithoutExplicitOptIn() {
    EngineExecutionResults result = EngineTestKit.engine("junit-jupiter")
        .selectors(selectClass(Sequential.class), selectClass(Failing.class))
        .configurationParameter("rfr.probe.enabled", "false").execute();
    assertTrue(result.testEvents().started().count() == 0 && result.containerEvents().skipped().count() == 2
        && complete(), "Queue probes ran without explicit opt-in or changed the pool");
  }

  private EngineExecutionResults execute(Class<?> target) {
    return EngineTestKit.engine("junit-jupiter").selectors(selectClass(target))
        .configurationParameter("rfr.probe.enabled", "true")
        .configurationParameter("junit.jupiter.execution.parallel.enabled", "false").execute();
  }

  private List<String> users(EngineExecutionResults result) {
    return result.testEvents().reportingEntryPublished().stream()
        .map(event -> event.getRequiredPayload(ReportEntry.class).getKeyValuePairs().get("users")).toList();
  }

  private boolean complete() {
    try (UserFixture first = new StaticUserFixture(Duration.ofMillis(73));
        UserFixture second = new StaticUserFixture(Duration.ofMillis(73));
        UserFixture third = new StaticUserFixture(Duration.ofMillis(73));
        UserFixture fourth = new StaticUserFixture(Duration.ofMillis(73))) {
      first.create();
      second.create();
      third.create();
      try {
        fourth.create();
        return false;
      } catch (IllegalStateException exception) {
        return exception.getMessage().contains("Timed out")
            && List.of(first.user().username(), second.user().username(), third.user().username())
                .stream().distinct().count() == 3
            && Map.of(first.user().username(), first.user().password(), second.user().username(),
                second.user().password(), third.user().username(), third.user().password())
                .equals(Map.of("pool_amber", "Amber7391", "pool_cobalt", "Cobalt2846", "pool_jade", "Jade6158"));
      }
    }
  }

  /** Последовательная мишень исчерпала бы пул без возврата пользователей из Store. */
  @ProbeTarget
  @Execution(ExecutionMode.SAME_THREAD)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  public static class Sequential {
    @Test
    @Order(1)
    void first(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("users", user.username());
    }
    @Test
    @Order(2)
    void second(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("users", user.username());
    }
    @Test
    @Order(3)
    void third(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("users", user.username());
    }
    @Test
    @Order(4)
    void fourth(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("users", user.username());
    }
  }

  /** После ошибки следующая мишень должна получить весь пул, включая возвращённого пользователя. */
  @ProbeTarget
  @Execution(ExecutionMode.SAME_THREAD)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  public static class Failing {
    @Test
    @Order(1)
    void fails(TestReporter reporter, @StaticUser TestUser user) {
      reporter.publishEntry("users", user.username());
      throw new AssertionError("Planned queue probe failure");
    }
    @Test
    @Order(2)
    void leasesAllUsers(TestReporter reporter, @StaticUser TestUser first,
        @StaticUser TestUser second, @StaticUser TestUser third) {
      reporter.publishEntry("users", first.username() + "," + second.username() + "," + third.username());
    }
  }
}
