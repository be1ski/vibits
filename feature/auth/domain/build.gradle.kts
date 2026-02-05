plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.utils)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.platform.testing)
        implementation(projects.feature.auth.domain.testing)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
