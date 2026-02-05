package space.be1ski.vibits.core.platform.locale

expect class LocaleProvider() {
  fun getSystemLocale(): String

  /** @return true if a restart is required for the change to take effect */
  fun configureLocale(language: AppLanguage): Boolean
}
