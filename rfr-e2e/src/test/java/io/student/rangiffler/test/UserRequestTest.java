package io.student.rangiffler.test;

import io.student.rangiffler.annotation.User;
import io.student.rangiffler.data.UserRequest;
import java.lang.annotation.Annotation;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserRequestTest {

  @ParameterizedTest
  @ValueSource(ints = {-1, -29, Integer.MIN_VALUE})
  void cannotRequestNegativeFriends(int friends) {
    assertThrows(IllegalArgumentException.class,
        () -> new UserRequest(friends, new Random().nextInt(0, Integer.MAX_VALUE)),
        "Negative friends count was not rejected");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -47, Integer.MIN_VALUE})
  void cannotRequestNegativeIncomeInvitations(int invitations) {
    assertThrows(IllegalArgumentException.class,
        () -> new UserRequest(new Random().nextInt(0, Integer.MAX_VALUE), invitations),
        "Negative incoming invitations count was not rejected");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -83, Integer.MIN_VALUE})
  void cannotRequestNegativeFriendsFromAnnotation(int friends) {
    assertThrows(IllegalArgumentException.class,
        () -> new UserRequest(new Counts(friends, new Random().nextInt(0, Integer.MAX_VALUE))),
        "Negative friends count from annotation was not rejected");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -61, Integer.MIN_VALUE})
  void cannotRequestNegativeIncomeInvitationsFromAnnotation(int invitations) {
    assertThrows(IllegalArgumentException.class,
        () -> new UserRequest(new Counts(new Random().nextInt(0, Integer.MAX_VALUE), invitations)),
        "Negative incoming invitations count from annotation was not rejected");
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "7, 0", "0, 13", "17, 23", "2147483647, 2147483647"})
  void nonnegativeRequestCanPrepareSingleUserFixture(int friends, int invitations) {
    assertDoesNotThrow(() -> new UserRequest(friends, invitations).fixture().close(),
        "Nonnegative request could not prepare the current single-user fixture");
  }

  @ParameterizedTest
  @CsvSource({"0, 0", "11, 0", "0, 19", "31, 43", "2147483647, 2147483647"})
  void nonnegativeAnnotationCanPrepareSingleUserFixture(int friends, int invitations) {
    assertDoesNotThrow(() -> new UserRequest(new Counts(friends, invitations)).fixture().close(),
        "Nonnegative annotation could not prepare the current single-user fixture");
  }

  private record Counts(int friends, int incomeInvitations) implements User {
    @Override
    public Class<? extends Annotation> annotationType() {
      return User.class;
    }
  }
}
