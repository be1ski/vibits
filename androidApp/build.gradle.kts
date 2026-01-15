val appVersion: String = providers.gradleProperty("appVersion").getOrElse("1.0.0")

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "space.be1ski.vibits.android"
  compileSdk = 36

  defaultConfig {
    applicationId = "space.be1ski.vibits.android"
    minSdk = 31
    targetSdk = 36
    versionCode = appVersion.replace(".", "").toIntOrNull() ?: 1
    versionName = appVersion
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: rootProject.file("release.keystore").absolutePath
      storeFile = file(keystorePath)
      storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "changeit"
      keyAlias = System.getenv("KEY_ALIAS") ?: "vibits"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "changeit"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
  buildFeatures {
    compose = true
  }
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.koin.android)
  implementation(libs.material.components)
  implementation(platform(libs.androidx.compose.bom))
  implementation(project(":shared"))
  debugImplementation(libs.androidx.compose.ui.tooling)
}
