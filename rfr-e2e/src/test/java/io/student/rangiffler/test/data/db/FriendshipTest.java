package io.student.rangiffler.test.data.db;

import io.student.rangiffler.data.db.Friendship;
import io.student.rangiffler.data.db.Profile;
import io.student.rangiffler.fixture.Cleanup;
import io.student.rangiffler.fixture.TearDown;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Создание и удаление дружеской связи")
public class FriendshipTest {

  @Test
  @DisplayName("Принятая дружеская связь создаётся в базе данных")
  void createsAcceptedFriendship() {
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    Profile requester = new Profile(first, "rfr_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile addressee = new Profile(second, "rfr_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Friendship friendship = new Friendship(UUID.randomUUID(), first, second, "ACCEPTED");
    try (Cleanup cleanup = new TearDown().with(requester::delete).with(addressee::delete)) {
      requester.create();
      addressee.create();
      friendship.create();
      assertTrue(friendship.exists(), "Created accepted friendship could not be found");
    }
  }

  @Test
  @DisplayName("Ожидающая дружеская связь удаляется из базы данных")
  void deletesPendingFriendship() {
    UUID first = UUID.randomUUID();
    UUID second = UUID.randomUUID();
    Profile requester = new Profile(first, "rfr_" + UUID.randomUUID().toString().replace("-", ""), "ru");
    Profile addressee = new Profile(second, "rfr_" + UUID.randomUUID().toString().replace("-", ""), "ru");
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
