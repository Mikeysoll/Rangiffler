package io.student.rangiffler.test.data.db;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProfileTest {

  @Test
  void createsProfileInRequestedCountry() {
    String name = "ui_" + UUID.randomUUID().toString().replace("-", "");
    Profile profile = new Profile(UUID.randomUUID(), name, "ru");
    try (Cleanup cleanup = new TearDown().with(profile::delete)) {
      profile.create();
      assertTrue(new Profile(name).exists(), "Created API profile could not be found by username");
    }
  }

  @Test
  void deletesProfileWithPendingInvitation() {
    UUID sender = UUID.randomUUID();
    UUID receiver = UUID.randomUUID();
    Profile profile = new Profile(sender, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile target = new Profile(receiver, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    try (Cleanup cleanup = new TearDown().with(profile::delete).with(target::delete)) {
      profile.create();
      target.create();
      new Friendship(UUID.randomUUID(), sender, receiver, "PENDING").create();
      profile.delete();
      assertFalse(profile.exists(), "Profile with pending invitation was not deleted");
    }
  }

  @Test
  void cannotCreateProfileForMissingCountry() {
    Profile profile = new Profile(UUID.randomUUID(), "ui_" + UUID.randomUUID().toString().replace("-", ""),
        "missing_" + UUID.randomUUID().toString().substring(0, 8));
    try (Cleanup cleanup = new TearDown().with(profile::delete)) {
      assertThrows(IllegalStateException.class, profile::create, "Missing country silently prevented profile creation");
    }
  }
}
