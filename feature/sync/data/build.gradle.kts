plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    val nonWasmMain by creating {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.androidx.room.runtime)
        implementation(projects.feature.memos.data)
      }
    }

    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.utils)
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.sync.domain)
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
      }
    }

    commonTest {
      dependencies {
        implementation(projects.feature.auth.domain.testing)
      }
    }

    androidMain {
      dependsOn(nonWasmMain)
    }

    desktopMain {
      dependsOn(nonWasmMain)
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
      }
    }

    iosMain {
      dependsOn(nonWasmMain)
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
      }
    }
  }
}
