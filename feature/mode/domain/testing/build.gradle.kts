plugins {
  id("vibits.kmp.library")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.feature.mode.domain)
        implementation(projects.core.platform)
      }
    }
  }
}
