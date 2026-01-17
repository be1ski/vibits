import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.appdistribution) apply false
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

  plugins.withId("org.jlleitschuh.gradle.ktlint") {
    extensions.configure<KtlintExtension> {
      version.set(rootProject.libs.versions.ktlintLib.get())
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

tasks.register("checkAll") {
  group = "verification"
  description = "Runs all checks: ktlint, detekt, compile, and tests"
  dependsOn(
    ":shared:ktlintCheck",
    ":shared:detekt",
    ":shared:compileKotlinDesktop",
    ":shared:desktopTest",
  )
}

tasks.register<Copy>("installGitHooks") {
  group = "setup"
  description = "Installs git pre-commit hook"
  from("scripts/pre-commit")
  into(".git/hooks")
  fileMode = 0b111101101 // 755
}
