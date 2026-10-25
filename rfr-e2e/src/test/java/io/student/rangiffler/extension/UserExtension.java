package io.student.rangiffler.extension;

import io.student.rangiffler.annotation.User;
import io.student.rangiffler.data.UserRequest;
import io.student.rangiffler.fixture.UserFixture;
import io.student.rangiffler.model.TestUser;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class UserExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    if (!parameterContext.isAnnotated(User.class)) {
      return false;
    }
    if (parameterContext.getParameter().getType() != TestUser.class) {
      throw new ParameterResolutionException("@User поддерживает только параметры типа TestUser");
    }
    return true;
  }

  @Override
  public TestUser resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
    ExtensionContext.Store store = extensionContext.getStore(
        ExtensionContext.Namespace.create(UserExtension.class, extensionContext.getUniqueId()));
    var key = parameterContext.getParameter();
    UserFixture fixture = store.get(key, UserFixture.class);
    if (fixture == null) {
      fixture = new UserRequest(parameterContext.findAnnotation(User.class).orElseThrow()).fixture();
      store.put(key, fixture);
      fixture.create();
    }
    return fixture.user();
  }
}
