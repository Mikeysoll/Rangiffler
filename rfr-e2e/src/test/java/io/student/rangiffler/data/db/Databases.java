package io.student.rangiffler.data.db;

import io.student.rangiffler.config.TestConfig;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

public enum Databases {
  AUTH(TestConfig.AUTH_DB_URL, TestConfig.AUTH_DB_USERNAME, TestConfig.AUTH_DB_PASSWORD),
  API(TestConfig.API_DB_URL, TestConfig.API_DB_USERNAME, TestConfig.API_DB_PASSWORD);

  private final DataSource dataSource;
  private final PlatformTransactionManager transactionManager;

  Databases(String url, String username, String password) {
    dataSource = new DriverManagerDataSource(url, username, password);
    transactionManager = new DataSourceTransactionManager(dataSource);
  }

  public DataSource dataSource() {
    return dataSource;
  }

  public PlatformTransactionManager transactionManager() {
    return transactionManager;
  }
}
