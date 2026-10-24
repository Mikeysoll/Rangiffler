package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class ProfilePage {
  private final Page page;

  public ProfilePage(Page page) {
    this.page = page;
  }

  @Step("Проверить, что авторизован пользователь {username}")
  public ProfilePage shouldBelongTo(String username) {
    assertThat(page).hasURL(Pattern.compile(".*/profile$"));
    assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName("My profile").setExact(true))).isVisible();
    assertThat(page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username").setExact(true))).hasValue(username);
    return this;
  }
}
