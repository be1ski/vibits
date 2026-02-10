plugins {
  id("vibits.kmp.feature.presentation")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.sync.domain)
        implementation(libs.compose.material)
        implementation(libs.kotlinx.datetime)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.auth.domain.testing)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.sync.domain.testing)
      }
    }
  }
}
