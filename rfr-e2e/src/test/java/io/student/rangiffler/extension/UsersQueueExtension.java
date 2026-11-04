package io.student.rangiffler.extension;

import io.student.rangiffler.annotation.StaticUser;
import io.student.rangiffler.fixture.StaticUserFixture;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/** Передаёт владение арендованным пользователем пулa Store текущего теста. */
public final class UsersQueueExtension implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
    if (!parameter.isAnnotated(StaticUser.class)) {
      return false;
    }
    if (parameter.getParameter().getType() != TestUser.class) {
      throw new ParameterResolutionException("@StaticUser supports only TestUser parameters");
    }
    return true;
  }

  @Override
  public TestUser resolveParameter(ParameterContext parameter, ExtensionContext context) {
    ExtensionContext.Store store = context.getStore(
        ExtensionContext.Namespace.create(UsersQueueExtension.class, context.getUniqueId()));
    var key = parameter.getParameter();
    UserFixture fixture = store.get(key, UserFixture.class);
    if (fixture == null) {
      fixture = new StaticUserFixture();
      store.put(key, fixture);
      fixture.create();
    }
    return fixture.user();
  }
}
