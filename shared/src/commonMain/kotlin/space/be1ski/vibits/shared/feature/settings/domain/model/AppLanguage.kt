package space.be1ski.vibits.shared.feature.settings.domain.model

/**
 * Supported app languages.
 */
enum class AppLanguage(
  val localeCode: String?,
) {
  SYSTEM(null),
  ENGLISH("en"),
  SPANISH("es"),
  CHINESE("zh"),
  HINDI("hi"),
  ARABIC("ar"),
  PORTUGUESE("pt"),
  RUSSIAN("ru"),
  JAPANESE("ja"),
  GERMAN("de"),
  FRENCH("fr"),
}
