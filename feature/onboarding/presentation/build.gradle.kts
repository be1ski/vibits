plugins {
  id("vibits.kmp.feature.presentation")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.onboarding.domain)
        implementation(libs.kotlinx.datetime)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.onboarding.data)
        implementation(projects.feature.onboarding.domain.testing)
      }
    }
  }
}
