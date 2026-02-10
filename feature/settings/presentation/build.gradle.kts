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
        implementation(projects.feature.settings.domain)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.auth.domain.testing)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.mode.domain.testing)
        implementation(projects.feature.onboarding.domain)
        implementation(projects.feature.onboarding.domain.testing)
        implementation(projects.feature.settings.domain.testing)
      }
    }
  }
}
