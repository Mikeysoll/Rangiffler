package io.student.rangiffler.data.db;

import static io.student.rangiffler.data.db.Databases.API;

public final class Friendships implements Rows {
  private final String username;

  public Friendships(String username) {
    this.username = username;
  }

  @Override
  public void delete() {
    JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/delete-registered-friendships.sql"))
        .param("username", username).update();
  }

  @Override
  public boolean exists() {
    return JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/count-user-friendships.sql"))
        .param("username", username).query(Long.class).single() > 0;
  }
}
