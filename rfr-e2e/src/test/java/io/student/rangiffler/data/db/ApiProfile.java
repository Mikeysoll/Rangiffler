package io.student.rangiffler.data.db;

import java.util.UUID;

import static io.student.rangiffler.data.db.Databases.API;

public final class ApiProfile implements Row {
  private final UUID id;
  private final String username;

  public ApiProfile(UUID id, String username) {
    this.id = id;
    this.username = username;
  }

  @Override
  public void create() {
    int rows = JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/insert-profile.sql"))
        .param("id", id.toString())
        .param("username", username)
        .param("countryCode", "ru")
        .update();
    if (rows != 1) {
      throw new IllegalStateException("Expected one API profile for " + username + " but inserted " + rows);
    }
  }
}
