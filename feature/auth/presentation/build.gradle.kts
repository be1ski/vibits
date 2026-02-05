plugins {
  id("vibits.kmp.compose")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.strings)
        implementation(projects.feature.auth.domain)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.resources)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
      }
    }
  }
}
