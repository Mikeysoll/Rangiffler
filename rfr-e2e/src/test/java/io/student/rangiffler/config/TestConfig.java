package io.student.rangiffler.config;

import java.util.Locale;

public final class TestConfig {

  private static final String DEFAULT_FRONT_URL = "http://localhost:3001";
  private static final String DEFAULT_AUTH_URL = "http://localhost:9001";
  private static final String DEFAULT_API_URL = "http://localhost:8081";
  private static final String DEFAULT_AUTH_DB_URL = "jdbc:mysql://localhost:3306/rangiffler-auth?serverTimezone=UTC";
  private static final String DEFAULT_AUTH_DB_USERNAME = "root";
  private static final String DEFAULT_AUTH_DB_PASSWORD = "secret";
  private static final String DEFAULT_API_DB_URL = "jdbc:mysql://localhost:3306/rangiffler-api?serverTimezone=UTC";
  private static final String DEFAULT_API_DB_USERNAME = "root";
  private static final String DEFAULT_API_DB_PASSWORD = "secret";

  public static final String FRONT_URL = read("base.url", DEFAULT_FRONT_URL);
  public static final String AUTH_URL = read("auth.url", DEFAULT_AUTH_URL);
  public static final String API_URL = read("api.url", DEFAULT_API_URL);
  public static final String AUTH_DB_URL = read("auth.db.url", DEFAULT_AUTH_DB_URL);
  public static final String AUTH_DB_USERNAME = read("auth.db.username", DEFAULT_AUTH_DB_USERNAME);
  public static final String AUTH_DB_PASSWORD = read("auth.db.password", DEFAULT_AUTH_DB_PASSWORD);
  public static final String API_DB_URL = read("api.db.url", DEFAULT_API_DB_URL);
  public static final String API_DB_USERNAME = read("api.db.username", DEFAULT_API_DB_USERNAME);
  public static final String API_DB_PASSWORD = read("api.db.password", DEFAULT_API_DB_PASSWORD);

  private TestConfig() {
  }

  private static String read(String key, String fallback) {
    String property = System.getProperty("rfr." + key);
    if (property != null && !property.isBlank()) {
      return property.trim();
    }
    String environment = System.getenv("RFR_" + key.replace('.', '_').toUpperCase(Locale.ROOT));
    return environment != null && !environment.isBlank() ? environment.trim() : fallback;
  }
}
