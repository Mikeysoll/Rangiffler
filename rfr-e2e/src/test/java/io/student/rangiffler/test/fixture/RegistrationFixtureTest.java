package io.student.rangiffler.test.fixture;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Registration;
import io.student.rangiffler.fixture.RegistrationFixture;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Очистка данных фикстурой регистрации")
public class RegistrationFixtureTest {

  @Test
  @DisplayName("Фикстура удаляет учётную запись после успешного сценария")
  void removesAccountAfterSuccessfulScenario() {
    Registration registration = new RegistrationFixture("rfr_" + UUID.randomUUID().toString().replace("-", ""), "k93u7Wx");
    try (registration) {
      UUID id = UUID.randomUUID();
      new AuthAccount(id, registration.username(), registration.password()).create();
      new Profile(id, registration.username()).create();
    }
    assertFalse(new AuthAccount(registration.username()).exists() || new Profile(registration.username()).exists(),
        "Completed scenario left its account in the database: " + registration.username());
  }

  @Test
  void removesBothUsersWhenScenarioFailsAfterProfileCreation() {
    Registration registration = new RegistrationFixture("rfr_" + UUID.randomUUID().toString().replace("-", ""), "N7q34vX");
    boolean failed = false;
    try (registration) {
      UUID id = UUID.randomUUID();
      new AuthAccount(id, registration.username(), registration.password()).create();
      new Profile(id, registration.username()).create();
      throw new AssertionError("Planned failure after profile creation");
    } catch (AssertionError exception) {
      if (!exception.getMessage().equals("Planned failure after profile creation") || exception.getSuppressed().length > 0) {
        throw exception;
      }
      failed = true;
    }
    assertTrue(failed && !new AuthAccount(registration.username()).exists()
        && !new Profile(registration.username()).exists(),
        "Failed registration left its auth account or API profile");
  }

  @Test
  @DisplayName("Фикстура удаляет учётную запись при ошибке до создания профиля")
  void removesAccountWhenScenarioFailsBeforeProfileCreation() {
    Registration registration = new RegistrationFixture("rfr_" + UUID.randomUUID().toString().replace("-", ""), "p4Y8nK2");
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
  @DisplayName("Фикстура допускает ошибку сценария до создания учётной записи")
  void toleratesScenarioFailureBeforeAccountCreation() {
    Registration registration = new RegistrationFixture("rfr_" + UUID.randomUUID().toString().replace("-", ""), "x9B3vT6");
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
