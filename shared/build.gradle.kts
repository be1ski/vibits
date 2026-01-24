import org.gradle.testing.jacoco.tasks.JacocoReport
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
  jacoco
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
        implementation(compose.components.resources)
        implementation(compose.foundation)
        implementation(compose.material)
        implementation(compose.material3)
        implementation(compose.materialIconsExtended)
        implementation(compose.runtime)
        implementation(compose.ui)
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
  packageOfResClass = "space.be1ski.vibits.shared"
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

tasks.register<JacocoReport>("jacocoDesktopTestReport") {
  dependsOn("desktopTest")
  executionData.setFrom(fileTree(layout.buildDirectory).include("jacoco/desktopTest.exec"))
  classDirectories.setFrom(
    fileTree(layout.buildDirectory.dir("classes/kotlin/desktop")) {
      exclude("**/BuildConfig.*")
    },
  )
  sourceDirectories.setFrom(files("src/commonMain/kotlin", "src/desktopMain/kotlin"))
  reports {
    html.required.set(true)
    xml.required.set(true)
    csv.required.set(false)
  }
}
