plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.kotlinx.kover")
}

val buildConfig = the<org.gradle.accessors.dm.LibrariesForBuildConfig>()
val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

kotlin {
  android {
    namespace = project.androidNamespace()
    compileSdk = buildConfig.versions.compileSdk.get().toInt()
    minSdk = buildConfig.versions.minSdk.get().toInt()
    // Required for Compose Multiplatform resources to work with the new KMP Android plugin
    androidResources.enable = true
  }

  jvm("desktop")

  // NOTE: WasmJS/iOS targets are defined but DON'T COMPILE until Kotlin 2.3.20+
  // Metro's @ContributesBinding doesn't work cross-module on native/wasm.
  // See root build.gradle.kts for details.
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
  }

  iosX64()
  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonTest {
      dependencies {
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    allWarningsAsErrors.set(true)
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }
}

fun Project.androidNamespace(): String {
  val pathSegments = path.split(":").filter { it.isNotEmpty() }
  val packagePrefix = providers.gradleProperty("vibits.packagePrefix").get()
  return "$packagePrefix.${pathSegments.joinToString(".")}"
}
