package io.student.rangiffler.test.fixture;

import io.student.rangiffler.fixture.TearDown;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет независимость шагов очистки и сохранение ошибки первого шага. */
public class TearDownTest {
  @Test
  void authCleanupCannotBeSkippedAfterApiCleanupFails() {
    List<String> attempts = new ArrayList<>();
    boolean preserved = false;
    try {
      new TearDown().with(() -> {
        attempts.add("api");
        throw new IllegalStateException("Planned API cleanup failure");
      }).with(() -> attempts.add("auth")).close();
    } catch (IllegalStateException exception) {
      preserved = exception.getCause().getMessage().equals("Planned API cleanup failure");
    }
    assertTrue(preserved && attempts.equals(List.of("api", "auth")),
        "API cleanup failure skipped auth cleanup or lost the original error");
  }
}
