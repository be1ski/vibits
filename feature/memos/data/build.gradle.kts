plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
  id("vibits.kmp.room")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    val roomMain = create("roomMain") {
      dependsOn(commonMain.get())
      dependencies {
        implementation(libs.androidx.room.runtime)
      }
    }

    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.ui)
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.mode.domain)
        implementation(projects.feature.sync.domain)
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
      dependsOn(roomMain)
    }

    desktopMain {
      dependsOn(roomMain)
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
      }
    }

    iosMain {
      dependsOn(roomMain)
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
      }
    }

    wasmJsMain {
      dependencies {
        implementation(libs.kotlinx.browser)
      }
    }
  }
}
