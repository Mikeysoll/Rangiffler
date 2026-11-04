package io.student.rangiffler.test.extension.probe;

import io.student.rangiffler.fixture.UserFixture;
import java.util.ArrayList;
import java.util.List;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/** Наблюдает вызовы create только в потоке выполнения одного управляющего теста. */
@Aspect("percflow(execution(* io.student.rangiffler.test.extension.UserExtensionTest.*(..))"
    + " && @annotation(org.junit.jupiter.api.Test))")
public final class UserCreation implements CreationObservation {
  private final List<String> usernames;

  public UserCreation() {
    usernames = new ArrayList<>();
  }

  @Override
  @Before("execution(void io.student.rangiffler.fixture.SingleUserFixture.create()) && this(fixture)")
  public void creating(UserFixture fixture) {
    usernames.add(fixture.user().username());
  }

  @Override
  public List<String> usernames() {
    return List.copyOf(usernames);
  }
}
