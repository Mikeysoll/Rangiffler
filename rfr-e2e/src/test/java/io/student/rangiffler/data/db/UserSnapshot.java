package io.student.rangiffler.data.db;

import java.util.List;

/** Фиксирует идентификаторы пользователей обеих схем и связей без чтения секретов. */
public record UserSnapshot(List<String> accounts, List<String> profiles, List<String> relations) {
  public UserSnapshot {
    accounts = List.copyOf(accounts);
    profiles = List.copyOf(profiles);
    relations = List.copyOf(relations);
  }

  public UserSnapshot(List<String> usernames) {
    this(JDBC.jdbcClient(Databases.AUTH).sql(JDBC.loadSql("sql/common/select-user-identities.sql"))
            .param("usernames", usernames).query(String.class).list(),
        JDBC.jdbcClient(Databases.API).sql(JDBC.loadSql("sql/common/select-user-identities.sql"))
            .param("usernames", usernames).query(String.class).list(),
        JDBC.jdbcClient(Databases.API).sql(JDBC.loadSql("sql/api/select-friendship-identities.sql"))
            .param("usernames", usernames).query(String.class).list());
  }
}
