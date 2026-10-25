package io.student.rangiffler.fixture;

import java.util.ArrayList;
import java.util.List;

public final class TearDown implements Cleanup {
  private final List<? extends AutoCloseable> resources;

  public TearDown(List<? extends AutoCloseable> resources) {
    this.resources = List.copyOf(resources);
  }

  public TearDown() {
    this(List.of());
  }

  @Override
  public TearDown with(AutoCloseable resource) {
    List<AutoCloseable> steps = new ArrayList<>(resources);
    steps.add(resource);
    return new TearDown(steps);
  }

  @Override
  public TearDown withEach(Iterable<? extends AutoCloseable> resources) {
    List<AutoCloseable> steps = new ArrayList<>(this.resources);
    resources.forEach(steps::add);
    return new TearDown(steps);
  }

  @Override
  public void close() {
    List<Exception> failures = new ArrayList<>();
    for (AutoCloseable resource : resources) {
      try {
        resource.close();
      } catch (Exception exception) {
        failures.add(exception);
      }
    }
    if (!failures.isEmpty()) {
      IllegalStateException failure = new IllegalStateException("Could not release all user fixtures", failures.getFirst());
      failures.stream().skip(1).forEach(failure::addSuppressed);
      throw failure;
    }
  }
}
