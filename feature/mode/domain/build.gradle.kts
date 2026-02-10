plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.onboarding.domain)
        implementation(projects.feature.settings.domain)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.auth.domain.testing)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.mode.domain.testing)
        implementation(projects.feature.onboarding.domain.testing)
        implementation(projects.feature.settings.domain.testing)
      }
    }
  }
}
