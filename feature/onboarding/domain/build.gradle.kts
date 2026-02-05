plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.memos.domain)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.ui)
        implementation(projects.feature.memos.data)
        implementation(projects.feature.memos.data.testing)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.onboarding.data)
        implementation(projects.feature.onboarding.domain.testing)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
