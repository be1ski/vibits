plugins {
  id("vibits.kmp.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

kotlin {
  sourceSets {
    val desktopTest by getting {
      dependencies {
        implementation(libs.compose.ui.test)
        implementation(compose.desktop.currentOs)
      }
    }
  }
}
