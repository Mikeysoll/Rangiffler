package io.student.rangiffler.data.db;

import java.util.List;

import static io.student.rangiffler.data.db.Databases.API;

public final class Friendships implements Relations {
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

  @Override
  public long friends() {
    return JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/count-friends.sql"))
        .param("username", username).query(Long.class).single();
  }

  @Override
  public long incomeInvitations() {
    return JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/count-income-invitations.sql"))
        .param("username", username).query(Long.class).single();
  }

  @Override
  public List<String> users() {
    return JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/select-related-usernames.sql"))
        .param("username", username).query(String.class).list();
  }
}
