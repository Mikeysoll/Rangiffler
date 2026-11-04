package io.student.rangiffler.fixture;

import io.student.rangiffler.model.TestUser;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/** Владеет единственной арендой пользователя пула до закрытия фикстуры из Store. */
public final class StaticUserFixture implements UserFixture {
  private static final BlockingQueue<Credentials> POOL = new ArrayBlockingQueue<>(3, true, List.of(
      new Credentials("pool_amber", "Amber7391"),
      new Credentials("pool_cobalt", "Cobalt2846"),
      new Credentials("pool_jade", "Jade6158")));
  private final Duration timeout;
  private Optional<TestUser> user;
  private boolean attempted;
  private boolean closed;

  private StaticUserFixture(Duration timeout, Optional<TestUser> user, boolean attempted, boolean closed) {
    this.timeout = timeout;
    this.user = user;
    this.attempted = attempted;
    this.closed = closed;
  }

  public StaticUserFixture(Duration timeout) {
    this(timeout, Optional.empty(), false, false);
    if (timeout.isNegative() || timeout.isZero()) {
      throw new IllegalArgumentException("Static user acquisition timeout must be positive");
    }
  }

  public StaticUserFixture() {
    this(Duration.ofSeconds(5));
  }

  @Override
  public synchronized void create() {
    if (attempted || closed) {
      throw new IllegalStateException("Static user fixture was already used or closed");
    }
    attempted = true;
    try {
      user = Optional.ofNullable(POOL.poll(timeout.toNanos(), TimeUnit.NANOSECONDS))
          .map(credentials -> new TestUser(UUID.nameUUIDFromBytes(credentials.username()
              .getBytes(StandardCharsets.UTF_8)), credentials.username(), credentials.password()));
      if (user.isEmpty()) {
        throw new IllegalStateException("Timed out acquiring a static user after " + timeout);
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while acquiring a static user", exception);
    }
  }

  @Override
  public synchronized TestUser user() {
    return user.orElseThrow(() -> new IllegalStateException("Static user fixture has no acquired user"));
  }

  @Override
  public synchronized void close() {
    if (closed) {
      return;
    }
    user.ifPresent(account -> {
      if (!POOL.offer(new Credentials(account.username(), account.password()))) {
        throw new IllegalStateException("Cannot return static user " + account.username() + " because the pool is full");
      }
    });
    user = Optional.empty();
    closed = true;
  }

  /** Хранит только заранее заданную пару учётных данных без обращения к стенду. */
  private record Credentials(String username, String password) {
  }
}
