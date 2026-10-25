package io.student.rangiffler.data.db;

import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import static io.student.rangiffler.data.db.Databases.AUTH;

public final class AuthAccount implements Row {
  private final String username;
  private final Optional<UUID> id;
  private final Optional<String> password;

  private AuthAccount(String username, Optional<UUID> id, Optional<String> password) {
    this.username = username;
    this.id = id;
    this.password = password;
  }

  public AuthAccount(UUID id, String username, String password) {
    this(username, Optional.of(id), Optional.of(password));
  }

  public AuthAccount(String username) {
    this(username, Optional.empty(), Optional.empty());
  }

  @Override
  public void create() {
    UUID identity = id.orElseThrow(() -> new IllegalStateException("Cannot create auth account without id for " + username));
    String encoded = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(
        password.orElseThrow(() -> new IllegalStateException("Cannot create auth account without password for " + username)));
    JDBC.inTransaction(AUTH, () -> {
      JDBC.requireRows(JDBC.jdbcClient(AUTH).sql(JDBC.loadSql("sql/auth/insert-user.sql"))
          .param("id", identity.toString()).param("username", username).param("password", encoded).update(),
          1, "Creating auth account " + username);
      JDBC.requireRows(JDBC.jdbcClient(AUTH).sql(JDBC.loadSql("sql/auth/insert-authorities.sql"))
          .param("id", identity.toString()).update(), 2, "Creating auth authorities for " + username);
    });
  }

  @Override
  public void delete() {
    JDBC.inTransaction(AUTH, () -> {
      JDBC.jdbcClient(AUTH).sql(JDBC.loadSql("sql/auth/delete-registered-authorities.sql")).param("username", username).update();
      JDBC.jdbcClient(AUTH).sql(JDBC.loadSql("sql/auth/delete-registered-user.sql")).param("username", username).update();
    });
  }

  @Override
  public boolean exists() {
    return JDBC.jdbcClient(AUTH).sql(JDBC.loadSql("sql/common/count-user-by-username.sql"))
        .param("username", username).query(Long.class).single() > 0;
  }
}
