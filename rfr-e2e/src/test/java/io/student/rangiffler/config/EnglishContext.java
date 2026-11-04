package io.student.rangiffler.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

/** Задаёт английскую локаль для проверки текстов ошибок авторизации. */
public final class EnglishContext implements OptionsFactory {
  @Override
  public Options getOptions() {
    return new Options().setContextOptions(new Browser.NewContextOptions().setLocale("en-US"));
  }
}
