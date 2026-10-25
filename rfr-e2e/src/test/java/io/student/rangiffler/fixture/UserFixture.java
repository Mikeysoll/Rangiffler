package io.student.rangiffler.fixture;

import io.student.rangiffler.model.TestUser;

public interface UserFixture extends AutoCloseable {
  void create();
  TestUser user();
  @Override
  void close();
}
