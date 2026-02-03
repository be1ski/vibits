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
      }
    }

    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.memos.data)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.sync.domain)
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.datetime)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.serialization.kotlinx.json)
      }
    }

    commonTest {
      dependencies {
        implementation(projects.feature.main)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.ktor.client.mock)
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
