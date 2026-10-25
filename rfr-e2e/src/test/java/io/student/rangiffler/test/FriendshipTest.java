package io.student.rangiffler.test;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FriendshipTest {

  @Test
  void createsAcceptedFriendship() {
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    Profile requester = new Profile(first, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile addressee = new Profile(second, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Friendship friendship = new Friendship(UUID.randomUUID(), first, second, "ACCEPTED");
    try (Cleanup cleanup = new TearDown().with(requester::delete).with(addressee::delete)) {
      requester.create();
      addressee.create();
      friendship.create();
      assertTrue(friendship.exists(), "Created accepted friendship could not be found");
    }
  }

  @Test
  void deletesPendingFriendship() {
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    Profile requester = new Profile(first, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile addressee = new Profile(second, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Friendship friendship = new Friendship(UUID.randomUUID(), first, second, "PENDING");
    try (Cleanup cleanup = new TearDown().with(requester::delete).with(addressee::delete)) {
      requester.create();
      addressee.create();
      friendship.create();
      friendship.delete();
      assertFalse(friendship.exists(), "Deleted pending friendship still exists");
    }
  }
}
