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
    int rows = JDBC.jdbcClient(API).sql(JDBC.loadSql("sql/api/insert-friendship.sql"))
        .param("id", id.toString())
        .param("requesterId", requester.toString())
        .param("addresseeId", addressee.toString())
        .param("status", status)
        .update();
    if (rows != 1) {
      throw new IllegalStateException("Expected one friendship " + id + " but inserted " + rows);
    }
  }
}
