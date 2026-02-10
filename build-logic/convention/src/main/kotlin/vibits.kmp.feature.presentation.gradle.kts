plugins {
  id("vibits.kmp.compose")
  id("vibits.kmp.metro")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(project(":core:elm"))
        implementation(project(":core:platform"))
        implementation(project(":core:strings"))
        implementation(project(":core:ui"))
        implementation(project(":core:utils"))
        implementation(libs.compose.foundation)
        implementation(libs.compose.material.icons.extended)
        implementation(libs.compose.material3)
        implementation(libs.compose.resources)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    commonTest {
      dependencies {
        implementation(project(":core:elm:test"))
      }
    }
    val desktopTest by getting {
      dependencies {
        implementation(project(":core:ui:testing"))
      }
    }
  }
}
