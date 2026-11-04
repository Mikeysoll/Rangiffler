package io.student.rangiffler.test.utils;

import io.student.rangiffler.utils.RandomDataUtils;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Проверяет соглашение об именах и изоляцию генерируемых тестовых пользователей. */
public class RandomDataUtilsTest {
  @Test
  void generatesDistinctUsernamesWithRequiredPrefix() {
    List<String> names = IntStream.range(0, 127).mapToObj(index -> RandomDataUtils.randomUsername()).toList();
    assertTrue(names.stream().distinct().count() == 127
        && names.stream().allMatch(name -> name.matches("rfr_[0-9a-f]{32}")),
        "Generated usernames reused an identity or violated the required rfr_ prefix");
  }
}
