plugins {
  id("vibits.kmp.compose")
}

kotlin {
  sourceSets {
    val desktopMain by getting {
      dependencies {
        api(libs.compose.ui.test)
        api(projects.core.ui)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
      }
    }
  }
}
