package io.student.rangiffler.config;

import java.util.Locale;

public final class TestConfig {

  private static final String DEFAULT_FRONT_URL = "http://localhost:3001";
  private static final String DEFAULT_AUTH_URL = "http://localhost:9001";
  private static final String DEFAULT_API_URL = "http://localhost:8081";

  public static final String FRONT_URL = read("base.url", DEFAULT_FRONT_URL);
  public static final String AUTH_URL = read("auth.url", DEFAULT_AUTH_URL);
  public static final String API_URL = read("api.url", DEFAULT_API_URL);

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
