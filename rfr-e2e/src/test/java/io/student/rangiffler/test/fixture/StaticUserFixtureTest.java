package io.student.rangiffler.test.fixture;

import io.student.rangiffler.fixture.StaticUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет исключительную выдачу и возврат пользователей без подключения к стенду. */
@Isolated("Owns the complete static user pool")
@Execution(ExecutionMode.SAME_THREAD)
public class StaticUserFixtureTest {
  @Test
  void cannotAcquireFromExhaustedPool() {
    boolean timedout = false;
    try (UserFixture first = new StaticUserFixture(Duration.ofMillis(83));
        UserFixture second = new StaticUserFixture(Duration.ofMillis(83));
        UserFixture third = new StaticUserFixture(Duration.ofMillis(83));
        UserFixture fourth = new StaticUserFixture(Duration.ofMillis(83))) {
      first.create();
      second.create();
      third.create();
      try {
        fourth.create();
      } catch (IllegalStateException exception) {
        timedout = exception.getMessage().contains("Timed out") && exception.getMessage().contains("PT0.083S");
      }
    }
    assertTrue(timedout && complete(), "Exhausted pool did not time out or returned an unacquired user");
  }

  @Test
  void cannotReturnUnacquiredUser() {
    try (UserFixture fixture = new StaticUserFixture(Duration.ofMillis(37))) {
      fixture.close();
      fixture.close();
    }
    assertTrue(complete(), "Closing an unacquired fixture changed the pool");
  }

  @Test
  void cannotReturnAcquiredUserTwice() {
    try (UserFixture fixture = new StaticUserFixture(Duration.ofMillis(41))) {
      fixture.create();
      fixture.close();
      fixture.close();
    }
    assertTrue(complete(), "Repeated close duplicated a pooled user");
  }

  @Test
  void cannotAcquireTwiceFromOneFixture() {
    try (UserFixture fixture = new StaticUserFixture(Duration.ofMillis(59))) {
      fixture.create();
      assertThrows(IllegalStateException.class, fixture::create,
          "Repeated create acquired another pooled user");
    }
  }

  @Test
  void concurrentLeasesCannotShareUser() throws Exception {
    CyclicBarrier barrier = new CyclicBarrier(3);
    List<String> users;
    try (var executor = Executors.newFixedThreadPool(3)) {
      List<Future<String>> futures = IntStream.range(0, 3).mapToObj(index -> executor.submit(() -> {
        try (UserFixture fixture = new StaticUserFixture(Duration.ofSeconds(2))) {
          fixture.create();
          String username = fixture.user().username();
          barrier.await(3, TimeUnit.SECONDS);
          return username;
        }
      })).toList();
      users = List.of(futures.get(0).get(5, TimeUnit.SECONDS), futures.get(1).get(5, TimeUnit.SECONDS),
          futures.get(2).get(5, TimeUnit.SECONDS));
    }
    assertTrue(users.stream().distinct().count() == 3 && complete(),
        "Concurrent leases shared a user or failed to restore the pool");
  }

  @Test
  void interruptedAcquisitionRestoresInterruptFlag() throws Exception {
    boolean interrupted;
    try (var executor = Executors.newSingleThreadExecutor()) {
      interrupted = executor.submit(() -> {
        try (UserFixture fixture = new StaticUserFixture(Duration.ofMillis(97))) {
          Thread.currentThread().interrupt();
          try {
            fixture.create();
            return false;
          } catch (IllegalStateException exception) {
            return exception.getCause() instanceof InterruptedException && Thread.currentThread().isInterrupted();
          } finally {
            Thread.interrupted();
          }
        }
      }).get(5, TimeUnit.SECONDS);
    }
    assertTrue(interrupted && complete(), "Interrupted acquisition lost its interrupt flag or changed the pool");
  }

  private boolean complete() {
    try (UserFixture first = new StaticUserFixture(Duration.ofMillis(61));
        UserFixture second = new StaticUserFixture(Duration.ofMillis(61));
        UserFixture third = new StaticUserFixture(Duration.ofMillis(61));
        UserFixture fourth = new StaticUserFixture(Duration.ofMillis(61))) {
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
}
