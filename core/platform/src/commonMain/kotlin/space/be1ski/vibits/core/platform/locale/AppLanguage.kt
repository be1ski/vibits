package space.be1ski.vibits.core.platform.locale

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
