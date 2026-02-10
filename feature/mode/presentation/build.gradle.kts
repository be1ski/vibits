plugins {
  id("vibits.kmp.feature.presentation")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.mode.domain)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.platform.testing)
        implementation(projects.feature.auth.domain.testing)
        implementation(projects.feature.mode.domain.testing)
      }
    }
  }
}
