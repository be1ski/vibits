plugins {
  id("vibits.kmp.compose")
}

kotlin {
  sourceSets {
    @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
    val desktopMain by getting {
      dependencies {
        api(compose.uiTest)
        api(projects.core.ui)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
      }
    }
  }
}
