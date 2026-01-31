plugins {
  id("vibits.kmp.library")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.serialization.kotlinx.json)
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
    androidMain {
      dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.core.ktx)
        implementation(libs.ktor.client.okhttp)
      }
    }
    desktopMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.ktor.client.cio)
        implementation(libs.slf4j.simple)
      }
    }
    iosMain {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
    wasmJsMain {
      dependencies {
        implementation(libs.kotlinx.browser)
        implementation(libs.ktor.client.js)
      }
    }
  }
}
