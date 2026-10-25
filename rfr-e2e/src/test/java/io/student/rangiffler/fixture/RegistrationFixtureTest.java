package io.student.rangiffler.fixture;

import io.student.rangiffler.data.AuthUserRepository;
import io.student.rangiffler.data.db.RegisteredUserRows;
import io.student.rangiffler.model.TestUser;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RegistrationFixtureTest {

  @Test
  void removesAccountAfterSuccessfulScenario() {
    Registration registration = new RegistrationFixture("ui_" + UUID.randomUUID().toString().replace("-", ""), "k93u7Wx");
    try (registration) {
      new AuthUserRepository().create(new TestUser(UUID.randomUUID(), registration.username(), registration.password()));
    }
    assertFalse(new RegisteredUserRows(registration.username()).getAsBoolean(), "Completed scenario left its account in the database: " + registration.username());
  }

  @Test
  void removesAccountWhenScenarioFailsBeforeProfileCreation() {
    Registration registration = new RegistrationFixture("ui_" + UUID.randomUUID().toString().replace("-", ""), "p4Y8nK2");
    try (registration) {
      new AuthUserRepository().create(new TestUser(UUID.randomUUID(), registration.username(), registration.password()));
      throw new AssertionError("Registration scenario interrupted before login");
    } catch (AssertionError expected) {
      if (expected.getSuppressed().length > 0) {
        throw expected;
      }
      assertFalse(new RegisteredUserRows(registration.username()).getAsBoolean(), "Failed scenario left its account in the database");
    }
  }

  @Test
  void toleratesScenarioFailureBeforeAccountCreation() {
    Registration registration = new RegistrationFixture("ui_" + UUID.randomUUID().toString().replace("-", ""), "x9B3vT6");
    try (registration) {
      throw new AssertionError("Registration scenario interrupted before submission");
    } catch (AssertionError expected) {
      if (expected.getSuppressed().length > 0) {
        throw expected;
      }
      assertFalse(new RegisteredUserRows(registration.username()).getAsBoolean(), "Unsubmitted registration unexpectedly left a database record");
    }
  }
}
