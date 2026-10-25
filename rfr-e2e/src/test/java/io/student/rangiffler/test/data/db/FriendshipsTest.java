package io.student.rangiffler.test.data.db;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Friendships;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FriendshipsTest {

  @Test
  void findsIncomingInvitation() {
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    String name = "ui_" + UUID.randomUUID().toString().replace("-", "");
    Profile requester = new Profile(first, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile addressee = new Profile(second, name, "ru");
    try (Cleanup cleanup = new TearDown().with(requester::delete).with(addressee::delete)) {
      requester.create();
      addressee.create();
      new Friendship(UUID.randomUUID(), first, second, "PENDING").create();
      assertTrue(new Friendships(name).exists(), "Incoming invitation could not be found for its addressee");
    }
  }

  @Test
  void deletesOwnRelationsWithoutDeletingUnrelatedFriendship() {
    UUID owner = UUID.randomUUID();
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    String name = "ui_" + UUID.randomUUID().toString().replace("-", "");
    Profile profile = new Profile(owner, name, "ru");
    Profile friend = new Profile(first, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile inviter = new Profile(second, "ui_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Friendship unrelated = new Friendship(UUID.randomUUID(), first, second, "PENDING");
    try (Cleanup cleanup = new TearDown().with(profile::delete).with(friend::delete).with(inviter::delete)) {
      profile.create();
      friend.create();
      inviter.create();
      new Friendship(UUID.randomUUID(), owner, first, "ACCEPTED").create();
      new Friendship(UUID.randomUUID(), second, owner, "PENDING").create();
      unrelated.create();
      new Friendships(name).delete();
      assertTrue(!new Friendships(name).exists() && unrelated.exists(), "Cleanup left own relations or deleted an unrelated friendship");
    }
  }
}
