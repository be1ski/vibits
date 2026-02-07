plugins {
  id("vibits.kmp.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
kotlin {
  sourceSets {
    val desktopTest by getting {
      dependencies {
        implementation(compose.uiTest)
        implementation(compose.desktop.currentOs)
      }
    }
  }
}
