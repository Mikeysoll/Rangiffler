package io.student.rangiffler.data;

import io.student.rangiffler.data.db.JDBC;
import io.student.rangiffler.model.TestUser;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.student.rangiffler.data.db.Databases.AUTH;

public final class AuthUserRepository {
  private static final String INSERT_USER = JDBC.loadSql("sql/auth/insert-user.sql");
  private static final String INSERT_AUTHORITIES = JDBC.loadSql("sql/auth/insert-authorities.sql");
  private static final String DELETE_AUTHORITIES = JDBC.loadSql("sql/auth/delete-authorities.sql");
  private static final String DELETE_USER = JDBC.loadSql("sql/auth/delete-user.sql");

  private final JdbcClient jdbc = JDBC.jdbcClient(AUTH);
  private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

  public void create(TestUser user) {
    String encodedPassword = passwordEncoder.encode(user.password());
    JDBC.inTransaction(AUTH, () -> {
      jdbc.sql(INSERT_USER)
          .param("id", user.id().toString())
          .param("username", user.username())
          .param("password", encodedPassword)
          .update();
      jdbc.sql(INSERT_AUTHORITIES).param("id", user.id().toString()).update();
    });
  }

  public void delete(TestUser user) {
    JDBC.inTransaction(AUTH, () -> {
      jdbc.sql(DELETE_AUTHORITIES).param("id", user.id().toString()).update();
      jdbc.sql(DELETE_USER).param("id", user.id().toString()).update();
    });
  }
}
