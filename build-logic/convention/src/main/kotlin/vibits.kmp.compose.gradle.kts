plugins {
  id("vibits.kmp.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
  sourceSets {
    val desktopTest by getting {
      dependencies {
        implementation(libs.findLibrary("compose-ui-test").get())
        implementation(compose.desktop.currentOs)
      }
    }
  }
}
