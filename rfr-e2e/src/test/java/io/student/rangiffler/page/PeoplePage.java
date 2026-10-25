package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.Header;
import java.util.Optional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class PeoplePage {
  public static String URL = TestConfig.FRONT_URL + "/people";

  private final Page page;
  private final Locator main;
  private final Locator panel;
  private final Header header;

  public PeoplePage(Page page) {
    this.page = page;
    main = page.getByRole(AriaRole.MAIN);
    panel = main.getByRole(AriaRole.TABPANEL);
    header = new Header(page);
  }

  @Step("Открыть вкладку друзей")
  public PeoplePage openFriends() {
    main.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions().setName("Friends").setExact(true)).click();
    return this;
  }

  @Step("Открыть входящие приглашения")
  public PeoplePage openIncomeInvitations() {
    main.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions().setName("Income invitations").setExact(true)).click();
    return this;
  }

  @Step("Открыть исходящие приглашения")
  public PeoplePage openOutcomeInvitations() {
    main.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions().setName("Outcome invitations").setExact(true)).click();
    return this;
  }

  @Step("Открыть всех пользователей")
  public PeoplePage openAllPeople() {
    main.getByRole(AriaRole.TAB, new Locator.GetByRoleOptions().setName("All People").setExact(true)).click();
    return this;
  }

  @Step("Найти пользователя {username}")
  public PeoplePage searchPeople(String username) {
    panel.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("search people").setExact(true)).fill(username);
    panel.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("search").setExact(true)).click();
    return this;
  }

  @Step("Отправить приглашение пользователю {username}")
  public PeoplePage sendInvitation(String username) {
    page.waitForResponse(response -> response.url().equals(TestConfig.API_URL + "/graphql")
        && Optional.ofNullable(response.request().postData()).orElse("").contains("FriendshipAction"),
        () -> row(username).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Add").setExact(true)).click());
    return this;
  }

  @Step("Принять приглашение пользователя {username}")
  public PeoplePage acceptInvitation(String username) {
    page.waitForResponse(response -> response.url().equals(TestConfig.API_URL + "/graphql")
        && Optional.ofNullable(response.request().postData()).orElse("").contains("FriendshipAction"),
        () -> row(username).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Accept").setExact(true)).click());
    return this;
  }

  @Step("Проверить количество пользователей: {count}")
  public PeoplePage checkThatUsersCount(int count) {
    assertThat(panel.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHas(page.getByRole(AriaRole.CELL)))).hasCount(count);
    return this;
  }

  @Step("Проверить исходящее приглашение пользователю {username}")
  public PeoplePage checkThatInvitationSentTo(String username) {
    assertThat(row(username).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Cancel").setExact(true))).isVisible();
    return this;
  }

  @Step("Проверить дружбу с пользователем {username}")
  public PeoplePage checkThatFriend(String username) {
    assertThat(row(username).getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Remove").setExact(true))).isVisible();
    return this;
  }

  @Step("Выйти из аккаунта со страницы пользователей")
  public LandingPage logout() {
    return header.logout();
  }

  private Locator row(String username) {
    return panel.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions()
        .setHas(page.getByRole(AriaRole.CELL, new Page.GetByRoleOptions().setName(username).setExact(true))));
  }
}
