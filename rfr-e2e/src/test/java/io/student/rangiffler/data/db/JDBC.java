package io.student.rangiffler.data.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.support.TransactionTemplate;

public final class JDBC {

  private JDBC() {
  }

  public static JdbcClient jdbcClient(Databases database) {
    return JdbcClient.create(database.dataSource());
  }

  public static <T> T inTransaction(Databases database, Supplier<T> action) {
    return new TransactionTemplate(database.transactionManager()).execute(status -> action.get());
  }

  public static void inTransaction(Databases database, Runnable action) {
    new TransactionTemplate(database.transactionManager()).executeWithoutResult(status -> action.run());
  }

  public static void requireRows(int actual, int expected, String context) {
    if (actual != expected) {
      throw new IllegalStateException(context + " expected " + expected + " rows but affected " + actual);
    }
  }

  public static String loadSql(String resourcePath) {
    try (var stream = new ClassPathResource(resourcePath).getInputStream()) {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Не удалось загрузить SQL-ресурс: " + resourcePath, e);
    }
  }
}
