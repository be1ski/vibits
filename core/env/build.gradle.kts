plugins {
  id("vibits.kmp.library")
  id("vibits.buildconfig")
}

generatedConfig {
  field("RELEASES_URL", providers.environmentVariable("CHANGELOG_RELEASES_URL").getOrElse(""))
}
