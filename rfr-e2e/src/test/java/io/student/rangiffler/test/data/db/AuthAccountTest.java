package io.student.rangiffler.test.data.db;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Создание и удаление учётных записей авторизации")
public class AuthAccountTest {

  @Test
  @DisplayName("Учётная запись создаётся с правами доступа и находится по имени пользователя")
  void createsAccountWithAuthorities() {
    String name = "rfr_" + UUID.randomUUID().toString().replace("-", "");
    AuthAccount account = new AuthAccount(UUID.randomUUID(), name, UUID.randomUUID().toString().substring(0, 11));
    try (Cleanup cleanup = new TearDown().with(account::delete)) {
      account.create();
      assertTrue(new AuthAccount(name).exists(), "Created auth account could not be found by username");
    }
  }

  @Test
  @DisplayName("Учётная запись удаляется вместе с правами доступа")
  void deletesAccountWithAuthorities() {
    AuthAccount account = new AuthAccount(UUID.randomUUID(), "rfr_" + UUID.randomUUID().toString().replace("-", ""),
        UUID.randomUUID().toString().substring(0, 9));
    try (Cleanup cleanup = new TearDown().with(account::delete)) {
      account.create();
      account.delete();
      assertFalse(account.exists(), "Deleted auth account still exists");
    }
  }

  @Test
  @DisplayName("Нельзя создать учётную запись без учётных данных")
  void cannotCreateLookupWithoutCredentials() {
    assertThrows(IllegalStateException.class, new AuthAccount("rfr_" + UUID.randomUUID().toString().replace("-", ""))::create,
        "Lookup-only auth account did not reject creation without credentials");
  }
}
