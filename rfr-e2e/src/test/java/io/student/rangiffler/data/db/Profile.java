package io.student.rangiffler.data.db;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.student.rangiffler.data.db.Databases.API;

public final class Profile implements Row {
  private final String username;
  private final Optional<UUID> id;
  private final String country;

  private Profile(String username, Optional<UUID> id, String country) {
    this.username = username;
    this.id = id;
    this.country = country;
  }

  public Profile(UUID id, String username, String country) {
    this(username, Optional.of(id), country);
  }

  public Profile(UUID id, String username) {
    this(id, username, "ru");
  }

  public Profile(String username) {
    this(username, Optional.empty(), "ru");
  }

  @Override
  public void create() {
    UUID identity = id.orElseThrow(() -> new IllegalStateException("Cannot create API profile without id for " + username));
    JDBC.requireRows(JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/insert-profile.sql"))
        .param("id", identity.toString()).param("username", username).param("countryCode", country).update(),
        1, "Creating API profile " + username);
  }

  @Override
  public void delete() {
    JDBC.inTransaction(API, () -> {
      new Friendships(username).delete();
      for (String resource : List.of("sql/api/delete-registered-photo-likes.sql", "sql/api/delete-registered-likes.sql",
          "sql/api/delete-registered-photos.sql", "sql/api/delete-registered-statistics.sql", "sql/api/delete-registered-user.sql")) {
        JDBC.jdbcClient(API).sql(JDBC.loadSql(resource)).param("username", username).update();
      }
    });
  }

  @Override
  public boolean exists() {
    return JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/common/count-user-by-username.sql"))
        .param("username", username).query(Long.class).single() > 0;
  }
}
