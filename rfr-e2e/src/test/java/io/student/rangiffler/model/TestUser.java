package io.student.rangiffler.model;

import java.util.UUID;
import java.util.List;

/** Пользователь теста с неизменяемым снимком исходящих приглашений. */
public record TestUser(UUID id, String username, String password, List<TestUser> friends,
    List<TestUser> incomeInvitations, List<TestUser> outcomeInvitations) {
  public TestUser {
    friends = List.copyOf(friends);
    incomeInvitations = List.copyOf(incomeInvitations);
    outcomeInvitations = List.copyOf(outcomeInvitations);
  }

  public TestUser(UUID id, String username, String password) {
    this(id, username, password, List.of());
  }

  public TestUser(UUID id, String username, String password, List<TestUser> outcomeInvitations) {
    this(id, username, password, List.of(), List.of(), outcomeInvitations);
  }

  public TestUser withFriends(List<TestUser> users) {
    return new TestUser(id, username, password, users, incomeInvitations, outcomeInvitations);
  }

  public TestUser withIncomeInvitations(List<TestUser> users) {
    return new TestUser(id, username, password, friends, users, outcomeInvitations);
  }

  public TestUser withOutcomeInvitations(List<TestUser> invitations) {
    return new TestUser(id, username, password, friends, incomeInvitations, invitations);
  }

  @Override
  public String toString() {
    return "TestUser[id=" + id + ", username=" + username + ", password=***]";
  }
}
