package io.student.rangiffler.test.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.SingleUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** Проверяет адресное удаление обоих пользователей и повторное закрытие фикстуры. */
public class SingleUserFixtureTest {
  @Test
  void repeatedClosureCannotLeaveAuthOrApiUser() {
    String username;
    try (UserFixture fixture = new SingleUserFixture()) {
      username = fixture.user().username();
      fixture.create();
      fixture.close();
      fixture.close();
    }
    assertFalse(new AuthAccount(username).exists() || new Profile(username).exists(),
        "Repeated fixture closure left the auth account or API profile");
  }
}
