plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.kotlinx.kover")
}

kotlin {
  androidLibrary {
    namespace = project.androidNamespace()
    compileSdk = 36
    minSdk = 31
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
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>().configureEach {
  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }
}

fun Project.androidNamespace(): String {
  val pathSegments = path.split(":").filter { it.isNotEmpty() }
  return "space.be1ski.vibits.${pathSegments.joinToString(".")}"
}
