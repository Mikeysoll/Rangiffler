package io.student.rangiffler.test.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Registration;
import io.student.rangiffler.fixture.RegistrationFixture;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RegistrationFixtureTest {

  @Test
  void removesAccountAfterSuccessfulScenario() {
    Registration registration = new RegistrationFixture("ui_" + UUID.randomUUID().toString().replace("-", ""), "k93u7Wx");
    try (registration) {
      new AuthAccount(UUID.randomUUID(), registration.username(), registration.password()).create();
    }
    assertFalse(new AuthAccount(registration.username()).exists() || new Profile(registration.username()).exists(),
        "Completed scenario left its account in the database: " + registration.username());
  }

  @Test
  void removesAccountWhenScenarioFailsBeforeProfileCreation() {
    Registration registration = new RegistrationFixture("ui_" + UUID.randomUUID().toString().replace("-", ""), "p4Y8nK2");
    try (registration) {
      new AuthAccount(UUID.randomUUID(), registration.username(), registration.password()).create();
      throw new AssertionError("Registration scenario interrupted before login");
    } catch (AssertionError expected) {
      if (expected.getSuppressed().length > 0) {
        throw expected;
      }
      assertFalse(new AuthAccount(registration.username()).exists() || new Profile(registration.username()).exists(),
          "Failed scenario left its account in the database");
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
      assertFalse(new AuthAccount(registration.username()).exists() || new Profile(registration.username()).exists(),
          "Unsubmitted registration unexpectedly left a database record");
    }
  }
}
