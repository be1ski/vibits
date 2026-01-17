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
  UKRAINIAN("uk"),
  BELARUSIAN("be"),
  KAZAKH("kk"),
  UZBEK("uz"),
  GEORGIAN("ka"),
  AZERBAIJANI("az"),
  KYRGYZ("ky"),
  TAJIK("tg"),
  ROMANIAN("ro"),
  TURKMEN("tk"),
  JAPANESE("ja"),
  GERMAN("de"),
  FRENCH("fr"),
}
