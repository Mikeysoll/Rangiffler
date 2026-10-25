package io.student.rangiffler.test;

import io.student.rangiffler.data.db.AuthAccount;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthAccountTest {

  @Test
  void createsAccountWithAuthorities() {
    String name = "ui_" + UUID.randomUUID().toString().replace("-", "");
    AuthAccount account = new AuthAccount(UUID.randomUUID(), name, UUID.randomUUID().toString().substring(0, 11));
    try (Cleanup cleanup = new TearDown().with(account::delete)) {
      account.create();
      assertTrue(new AuthAccount(name).exists(), "Created auth account could not be found by username");
    }
  }

  @Test
  void deletesAccountWithAuthorities() {
    AuthAccount account = new AuthAccount(UUID.randomUUID(), "ui_" + UUID.randomUUID().toString().replace("-", ""),
        UUID.randomUUID().toString().substring(0, 9));
    try (Cleanup cleanup = new TearDown().with(account::delete)) {
      account.create();
      account.delete();
      assertFalse(account.exists(), "Deleted auth account still exists");
    }
  }

  @Test
  void cannotCreateLookupWithoutCredentials() {
    assertThrows(IllegalStateException.class, new AuthAccount("ui_" + UUID.randomUUID().toString().replace("-", ""))::create,
        "Lookup-only auth account did not reject creation without credentials");
  }
}
