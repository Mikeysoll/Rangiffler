package io.student.rangiffler.data.db;

import java.util.List;

public interface Relations extends Rows {
  long friends();
  long incomeInvitations();
  List<String> users();
}
