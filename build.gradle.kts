import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.firebase.appdistribution) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kover)
  alias(libs.plugins.ktlint) apply false
}

subprojects {
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
  }
  plugins.withId("org.jetbrains.kotlin.android") {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
  }
  plugins.withId("com.android.application") {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
  }

  plugins.withId("org.jlleitschuh.gradle.ktlint") {
    extensions.configure<KtlintExtension> {
      version.set(libs.versions.ktlintLib.get())
      filter {
        exclude { element ->
          element.file.path.contains("/build/")
        }
      }
    }
  }

  plugins.withId("io.gitlab.arturbosch.detekt") {
    extensions.configure<DetektExtension> {
      buildUponDefaultConfig = true
      allRules = true
      config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
    tasks.withType<Detekt>().configureEach {
      setSource(
        files(
          "src/commonMain/kotlin",
          "src/androidMain/kotlin",
          "src/desktopMain/kotlin",
          "src/iosMain/kotlin",
          "src/wasmJsMain/kotlin",
          "src/main/kotlin",
        ),
      )
    }
  }
}

// ===================================================================================
// iOS/WasmJS DISABLED: Waiting for Kotlin 2.3.20+
// ===================================================================================
// Metro's @ContributesBinding doesn't work cross-module on native/wasm targets.
// After modularization, DI bindings from feature modules (e.g., MemosRepositoryImpl)
// are not found by AppGraph on iOS/WasmJS, causing compilation errors like:
//   "Cannot find @Inject constructor or @Provides for: MemosRepository"
//
// This works on JVM/Android because Metro uses different code generation there.
// Fix available in Kotlin 2.3.20+: https://github.com/ZacSweers/metro/issues/460
//
// TODO(Kotlin 2.3.20): Re-enable iOS/WasmJS:
//   - Add compileKotlinIos* tasks to checkAll
//   - Add runKtlintCheckOverIosMainSourceSet, runKtlintCheckOverWasmJsMainSourceSet
//   - Add webApp checks: ktlintCheck, detekt, compileKotlinWasmJs
// ===================================================================================
tasks.register("checkAll") {
  group = "verification"
  description = "Runs all checks: ktlint, detekt, compile, and tests"

  // Dynamically find all KMP modules
  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      // Add ktlint for desktop/common/android source sets only (skip iOS/WasmJS)
      tasks.findByName("runKtlintCheckOverCommonMainSourceSet")?.let { dependsOn(it) }
      tasks.findByName("runKtlintCheckOverCommonTestSourceSet")?.let { dependsOn(it) }
      tasks.findByName("runKtlintCheckOverAndroidMainSourceSet")?.let { dependsOn(it) }
      tasks.findByName("runKtlintCheckOverDesktopMainSourceSet")?.let { dependsOn(it) }
      tasks.findByName("runKtlintCheckOverDesktopTestSourceSet")?.let { dependsOn(it) }
      // Add detekt for all KMP modules
      tasks.findByName("detekt")?.let { dependsOn(it) }
      // Add desktop tests for all KMP modules that have them
      tasks.findByName("desktopTest")?.let { dependsOn(it) }
    }
  }
  // Android app checks
  dependsOn(":androidApp:ktlintCheck", ":androidApp:detekt", ":androidApp:compileDebugKotlin")
  // Desktop app checks
  dependsOn(":desktopApp:ktlintCheck", ":desktopApp:detekt", ":desktopApp:compileKotlinDesktop")
}

tasks.register<Copy>("installGitHooks") {
  group = "setup"
  description = "Installs git pre-commit hook"
  from("scripts/pre-commit")
  into(".git/hooks")
  filePermissions {
    unix("rwxr-xr-x")
  }
}

dependencies {
  subprojects.forEach { subproject ->
    subproject.plugins.withId("org.jetbrains.kotlinx.kover") {
      kover(subproject)
    }
  }
}

kover {}
