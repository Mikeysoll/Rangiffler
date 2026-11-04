package io.student.rangiffler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;
import io.student.rangiffler.config.TestConfig;
import io.student.rangiffler.page.component.Header;
import com.microsoft.playwright.options.FilePayload;
import java.util.Optional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public final class ProfilePage {
  public static final String URL = TestConfig.FRONT_URL + "/profile";

  private final Page page;
  private final Locator usernameInput;
  private final Header header;
  private final Locator main;

  public ProfilePage(Page page) {
    this.page = page;
    usernameInput = page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Username").setExact(true));
    header = new Header(page);
    main = page.getByRole(AriaRole.MAIN);
  }

  @Step("Проверить, что авторизован пользователь {username}")
  public ProfilePage shouldBelongTo(String username) {
    assertThat(page).hasURL(URL);
    assertThat(usernameInput).hasValue(username);
    header.shouldAllowLogout();
    return this;
  }

  @Step("Заполнить профиль: имя {firstname}, фамилия {surname}")
  public ProfilePage fillProfileForm(String firstname, String surname) {
    main.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("First Name").setExact(true)).fill(firstname);
    main.getByRole(AriaRole.TEXTBOX, new Locator.GetByRoleOptions().setName("Surname").setExact(true)).fill(surname);
    return this;
  }

  @Step("Выбрать страну {country}")
  public ProfilePage selectLocation(String country) {
    main.getByRole(AriaRole.COMBOBOX, new Locator.GetByRoleOptions().setName("Location").setExact(true)).click();
    page.getByRole(AriaRole.OPTION).filter(new Locator.FilterOptions().setHas(
        page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName(country).setExact(true)))).click();
    return this;
  }

  @Step("Загрузить аватар {filename}: {image}")
  public ProfilePage uploadAvatar(String filename, byte[] image) {
    main.getByLabel("Update avatar", new Locator.GetByLabelOptions().setExact(true))
        .setInputFiles(new FilePayload(filename, "image/png", image));
    return this;
  }

  @Step("Сохранить изменения профиля")
  public ProfilePage save() {
    page.waitForResponse(response -> response.url().equals(TestConfig.API_URL + "/graphql")
        && response.request().method().equals("POST")
        && Optional.ofNullable(response.request().postData()).orElse("").contains("UpdateUser"),
        () -> main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Save").setExact(true)).click());
    return this;
  }

  @Step("Сбросить несохранённые изменения профиля")
  public ProfilePage reset() {
    main.getByRole(AriaRole.BUTTON, new Locator.GetByRoleOptions().setName("Reset").setExact(true)).click();
    return this;
  }

  @Step("Повторно загрузить профиль с сервера")
  public ProfilePage reload() {
    page.reload();
    return this;
  }

  @Step("Проверить профиль: имя {firstname}, фамилия {surname}, страна {country}")
  public ProfilePage checkThatProfile(String firstname, String surname, String country) {
    assertThat(main.getByRole(AriaRole.TEXTBOX,
        new Locator.GetByRoleOptions().setName("First Name").setExact(true))).hasValue(firstname);
    assertThat(main.getByRole(AriaRole.TEXTBOX,
        new Locator.GetByRoleOptions().setName("Surname").setExact(true))).hasValue(surname);
    assertThat(main.getByRole(AriaRole.COMBOBOX,
        new Locator.GetByRoleOptions().setName("Location").setExact(true))).hasText(country);
    return this;
  }

  @Step("Проверить загруженный аватар {data} шириной {width}")
  public ProfilePage checkThatAvatar(String data, int width) {
    Locator avatar = main.getByRole(AriaRole.IMG, new Locator.GetByRoleOptions().setName("").setExact(true));
    assertThat(avatar).isVisible();
    assertThat(avatar).hasAttribute("src", data);
    assertThat(avatar).hasJSProperty("naturalWidth", width);
    return this;
  }
}
