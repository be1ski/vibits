plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
}

kotlin {
  androidTarget()
  jvm("desktop")
  iosX64()
  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.kotlinx.datetime)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.ktor.client.logging)
        implementation(libs.koin.core)
        implementation(libs.koin.compose)
      }
    }
    val commonTest by getting
    val androidMain by getting {
      dependencies {
        implementation(libs.ktor.client.okhttp)
        implementation(libs.koin.android)
      }
    }
    val desktopMain by getting {
      dependencies {
        implementation(libs.ktor.client.cio)
        implementation(libs.kotlinx.coroutines.swing)
      }
    }
    val iosMain by creating {
      dependsOn(commonMain)
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
    val iosX64Main by getting { dependsOn(iosMain) }
    val iosArm64Main by getting { dependsOn(iosMain) }
    val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
  }
}

android {
  namespace = "space.be1ski.memos.shared"
  compileSdk = 36
  defaultConfig {
    minSdk = 31
  }
}
