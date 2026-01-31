plugins {
  id("vibits.kmp.library")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.elm.test)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
