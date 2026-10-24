package io.student.rangiffler.page;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.Header;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class MyTravelsPage {
  public static String URL = TestConfig.FRONT_URL + "/my-travels";

  private final Page page;
  private final Header header;

  public MyTravelsPage(Page page) {
    this.page = page;
    header = new Header(page);
  }

  @Step("Проверить успешную авторизацию на странице путешествий")
  public MyTravelsPage shouldBeAuthenticated() {
    assertThat(page).hasURL(URL);
    header.shouldAllowLogout();
    return this;
  }

  @Step("Открыть профиль")
  public ProfilePage openProfile() {
    return header.openProfile();
  }
}
