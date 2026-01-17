package space.be1ski.vibits.shared.feature.preferences.domain.model

/**
 * Supported app languages.
 */
enum class AppLanguage(
  val localeCode: String?,
) {
  SYSTEM(null),
  ENGLISH("en"),
  RUSSIAN("ru"),
}
