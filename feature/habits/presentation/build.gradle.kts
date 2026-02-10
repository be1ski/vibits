plugins {
  id("vibits.kmp.feature.presentation")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.memos.domain)
        implementation(libs.compose.material)
        implementation(libs.kotlinx.datetime)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.memos.domain.testing)
      }
    }
  }
}
