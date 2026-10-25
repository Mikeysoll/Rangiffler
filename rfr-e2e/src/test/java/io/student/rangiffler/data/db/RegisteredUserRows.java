package io.student.rangiffler.data.db;

import java.util.List;
import java.util.function.BooleanSupplier;

import static io.student.rangiffler.data.db.Databases.API;
import static io.student.rangiffler.data.db.Databases.AUTH;

public final class RegisteredUserRows implements Runnable, BooleanSupplier {
  private final String username;

  public RegisteredUserRows(String username) {
    this.username = username;
  }

  @Override
  public boolean getAsBoolean() {
    return exists(AUTH) || exists(API);
  }

  @Override
  public void run() {
    try (AutoCloseable auth = () -> delete(AUTH, List.of(
        "sql/auth/delete-registered-authorities.sql",
        "sql/auth/delete-registered-user.sql"))) {
      delete(API, List.of(
          "sql/api/delete-registered-photo-likes.sql",
          "sql/api/delete-registered-friendships.sql",
          "sql/api/delete-registered-likes.sql",
          "sql/api/delete-registered-photos.sql",
          "sql/api/delete-registered-statistics.sql",
          "sql/api/delete-registered-user.sql"));
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot clean up registered user " + username, exception);
    }
  }

  private boolean exists(Databases database) {
    return JDBC.jdbcClient(database)
        .sql(JDBC.loadSql("sql/common/count-user-by-username.sql"))
        .param("username", username)
        .query(Long.class)
        .single() > 0;
  }

  private void delete(Databases database, List<String> resources) {
    JDBC.inTransaction(database, () -> {
      for (String resource : resources) {
        JDBC.jdbcClient(database).sql(JDBC.loadSql(resource)).param("username", username).update();
      }
    });
  }
}
