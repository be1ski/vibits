plugins {
  id("vibits.kmp.compose")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.strings)
        implementation(libs.compose.resources)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
        implementation(libs.kotlinx.datetime)
      }
    }
    androidMain {
      dependencies {
        implementation(libs.androidx.activity.compose)
      }
    }
  }
}
