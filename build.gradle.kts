import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.appdistribution) apply false
}

subprojects {
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    apply(plugin = "io.gitlab.arturbosch.detekt")
  }
  plugins.withId("org.jetbrains.kotlin.android") {
    apply(plugin = "io.gitlab.arturbosch.detekt")
  }

  plugins.withId("io.gitlab.arturbosch.detekt") {
    extensions.configure<DetektExtension> {
      buildUponDefaultConfig = true
      allRules = true
      config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }
    tasks.withType<Detekt>().configureEach {
      setSource(files(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/desktopMain/kotlin",
        "src/iosMain/kotlin",
        "src/wasmJsMain/kotlin",
        "src/main/kotlin"
      ))
    }
  }
}
