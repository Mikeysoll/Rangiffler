package io.student.rangiffler.fixture;

public interface Registration extends AutoCloseable {
  String username();
  String password();
  @Override
  void close();
}
