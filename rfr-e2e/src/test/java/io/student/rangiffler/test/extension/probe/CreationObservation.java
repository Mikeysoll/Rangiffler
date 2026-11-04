package io.student.rangiffler.test.extension.probe;

import io.student.rangiffler.fixture.UserFixture;
import java.util.List;

/** Наблюдение за границей создания данных без владения самими ресурсами. */
public interface CreationObservation {
  void creating(UserFixture fixture);
  List<String> usernames();
}
