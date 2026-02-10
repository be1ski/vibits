plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.utils)
        implementation(projects.feature.settings.domain)
      }
    }
  }
}
