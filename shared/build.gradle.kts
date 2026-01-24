import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
  alias(libs.plugins.kotlin.multiplatform) // must be first
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.metro)
  alias(libs.plugins.kover)
}

kotlin {
  androidLibrary {
    namespace = "space.be1ski.vibits.shared"
    compileSdk = 36
    minSdk = 31

    androidResources {
      enable = true
    }
  }
  jvm("desktop")
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }
  val iosX64Target = iosX64()
  val iosArm64Target = iosArm64()
  val iosSimulatorArm64Target = iosSimulatorArm64()

  val xcframework = XCFramework()
  listOf(iosX64Target, iosArm64Target, iosSimulatorArm64Target).forEach { target ->
    target.binaries.framework {
      baseName = "shared"
      if (buildType == NativeBuildType.RELEASE) {
        freeCompilerArgs += listOf("-Xdisable-phases=Devirtualization")
      }
      xcframework.add(this)
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(libs.compose.foundation)
        implementation(libs.compose.material)
        implementation(libs.compose.material.icons.extended)
        implementation(libs.compose.material3)
        implementation(libs.compose.resources)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
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
    val roomMain by creating {
      dependsOn(commonMain)
      dependencies {
        implementation(libs.androidx.room.runtime)
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.ktor.client.mock)
        implementation(libs.kotlinx.coroutines.test)
      }
    }
    val androidMain by getting {
      dependsOn(roomMain)
      dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.core.ktx)
        implementation(libs.ktor.client.okhttp)
      }
    }
    val desktopMain by getting {
      dependsOn(roomMain)
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.ktor.client.cio)
        implementation(libs.slf4j.simple)
      }
    }
    val iosMain by creating {
      dependsOn(roomMain)
      dependencies {
        implementation(libs.androidx.sqlite.bundled)
        implementation(libs.ktor.client.darwin)
      }
    }
    val iosX64Main by getting { dependsOn(iosMain) }
    val iosArm64Main by getting { dependsOn(iosMain) }
    val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    val wasmJsMain by getting {
      dependencies {
        implementation(libs.ktor.client.js)
      }
    }
  }
}

compose.resources {
  packageOfResClass = "space.be1ski.vibits.shared.generated"
}

dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspDesktop", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
  add("kspIosX64", libs.androidx.room.compiler)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }
}

kover {
  currentProject {
    sources {
      excludedSourceSets.addAll("androidMain", "iosMain", "wasmJsMain", "roomMain")
    }
  }
  reports {
    filters {
      excludes {
        classes(
          // TEA data classes (State/Action/Effect/Features)
          "*State",
          "*State$*",
          "*Action",
          "*Action$*",
          "*Effect",
          "*Effect$*",
          "*Features",
          "*Features$*",
          // DI modules
          "*.di.*",
          // Generated code (Compose Resources, etc.)
          "*.generated.*",
          // Platform-specific code (expect/actual)
          "*.platform.*",
          // Room database (platform-specific persistence layer)
          "*.room.*",
          // UI layer (Compose components, theming, etc.)
          "*.ui.*",
          // View/UI components
          "*.view.*",
          // Data layer - DTO serialization classes
          "*Dto",
          "*Dto$*",
        )
      }
    }
  }
}
