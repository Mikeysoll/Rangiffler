package io.student.rangiffler.test.data.db;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Создание и удаление профилей пользователей")
public class ProfileTest {

  @Test
  @DisplayName("Профиль создаётся для указанной страны и находится по имени пользователя")
  void createsProfileInRequestedCountry() {
    String name = "rfr_" + UUID.randomUUID().toString().replace("-", "");
    Profile profile = new Profile(UUID.randomUUID(), name, "ru");
    try (Cleanup cleanup = new TearDown().with(profile::delete)) {
      profile.create();
      assertTrue(new Profile(name).exists(), "Created API profile could not be found by username");
    }
  }

  @Test
  @DisplayName("Профиль с ожидающим приглашением удаляется")
  void deletesProfileWithPendingInvitation() {
    UUID sender = UUID.randomUUID();
    UUID receiver = UUID.randomUUID();
    Profile profile = new Profile(sender, "rfr_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile target = new Profile(receiver, "rfr_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    try (Cleanup cleanup = new TearDown().with(profile::delete).with(target::delete)) {
      profile.create();
      target.create();
      new Friendship(UUID.randomUUID(), sender, receiver, "PENDING").create();
      profile.delete();
      assertFalse(profile.exists(), "Profile with pending invitation was not deleted");
    }
  }

  @Test
  @DisplayName("Нельзя создать профиль для отсутствующей страны")
  void cannotCreateProfileForMissingCountry() {
    Profile profile = new Profile(UUID.randomUUID(), "rfr_" + UUID.randomUUID().toString().replace("-", ""),
        "missing_" + UUID.randomUUID().toString().substring(0, 8));
    try (Cleanup cleanup = new TearDown().with(profile::delete)) {
      assertThrows(IllegalStateException.class, profile::create, "Missing country silently prevented profile creation");
    }
  }
}
