package io.student.rangiffler.fixture;

public interface Cleanup extends AutoCloseable {
  Cleanup with(AutoCloseable resource);
  Cleanup withEach(Iterable<? extends AutoCloseable> resources);
  @Override
  void close();
}
