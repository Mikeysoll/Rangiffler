package io.student.rangiffler.test.profile;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import io.student.rangiffler.annotation.User;
import io.student.rangiffler.model.TestUser;
import io.student.rangiffler.page.LandingPage;
import io.student.rangiffler.page.ProfilePage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Проверяет сохранение профиля на сервере и сброс несохранённых изменений. */
@UsePlaywright
@Tag("ui")
public class ProfileTest {
  @Test
  void savesChangedFirstname(Page page, @User TestUser user) {
    String firstname = "Mira-" + UUID.randomUUID();
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openProfile().fillProfileForm(firstname, "").save().reload()
        .checkThatProfile(firstname, "", "Russian Federation");
  }

  @Test
  void savesChangedSurname(Page page, @User TestUser user) {
    String surname = "Volkova-" + UUID.randomUUID();
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openProfile().fillProfileForm("", surname).save().reload()
        .checkThatProfile("", surname, "Russian Federation");
  }

  @Test
  void savesChangedLocation(Page page, @User TestUser user) {
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openProfile().selectLocation("Canada").save().reload().checkThatProfile("", "", "Canada");
  }

  @Test
  void savesUploadedAvatar(Page page, @User TestUser user) throws Exception {
    BufferedImage image = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
    image.setRGB(1, 1, 0x2a9b7f);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    if (!ImageIO.write(image, "png", buffer)) {
      throw new IllegalStateException("PNG encoder is unavailable");
    }
    String data = "data:image/png;base64," + Base64.getEncoder().encodeToString(buffer.toByteArray());
    new LandingPage(page).open().login().fillLoginForm(user.username(), user.password()).signIn()
        .openProfile().uploadAvatar("avatar.png", buffer.toByteArray()).checkThatAvatar(data, 3)
        .save().reload().checkThatAvatar(data, 3);
  }

  @Test
  void resetCannotPersistUnsavedChanges(Page page, @User TestUser user) {
    ProfilePage profile = new LandingPage(page).open().login().fillLoginForm(user.username(), user.password())
        .signIn().openProfile().fillProfileForm("Aida-73", "Stone-29").selectLocation("Canada")
        .save().reload().checkThatProfile("Aida-73", "Stone-29", "Canada");
    profile.fillProfileForm("Draft-41", "Unsaved-83").selectLocation("France").reset()
        .checkThatProfile("Aida-73", "Stone-29", "Canada").reload()
        .checkThatProfile("Aida-73", "Stone-29", "Canada");
  }
}
