plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.mode.domain)
      }
    }
    androidMain {
      dependencies {
        implementation(libs.androidx.core.ktx)
      }
    }
    wasmJsMain {
      dependencies {
        implementation(libs.kotlinx.browser)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.homescreen)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
