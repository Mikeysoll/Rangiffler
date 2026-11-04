package io.student.rangiffler.data.db;

import java.util.UUID;

import static io.student.rangiffler.data.db.Databases.API;

public final class Friendship implements Row {
  private final UUID id;
  private final UUID requester;
  private final UUID addressee;
  private final String status;

  public Friendship(UUID id, UUID requester, UUID addressee, String status) {
    this.id = id;
    this.requester = requester;
    this.addressee = addressee;
    this.status = status;
  }

  @Override
  public void create() {
    JDBC.requireRows(JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/insert-friendship.sql"))
        .param("id", id.toString())
        .param("requesterId", requester.toString())
        .param("addresseeId", addressee.toString())
        .param("status", status)
        .update(), 1, "Creating friendship " + id);
  }

  @Override
  public void delete() {
    JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/delete-friendship.sql")).param("id", id.toString()).update();
  }

  @Override
  public boolean exists() {
    return JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/count-friendship.sql"))
        .param("id", id.toString()).query(Long.class).single() > 0;
  }
}
