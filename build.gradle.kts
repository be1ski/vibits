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

tasks.register("verify") {
  group = "verification"
  description = "Runs JVM checks: detekt, compile, and tests (Linux-safe)"

  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("detekt")?.let { dependsOn(it) }
      tasks.findByName("desktopTest")?.let { dependsOn(it) }
    }
  }
  dependsOn(":androidApp:ktlintCheck", ":androidApp:detekt", ":androidApp:compileDebugKotlin")
  dependsOn(":desktopApp:ktlintCheck", ":desktopApp:detekt", ":desktopApp:compileKotlinDesktop")
  dependsOn(":webApp:ktlintCheck", ":webApp:detekt", ":webApp:compileKotlinWasmJs")
}

tasks.register("verifyNative") {
  group = "verification"
  description = "Runs native checks: iOS compile, ktlint (requires macOS)"

  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("compileKotlinIosArm64")?.let { dependsOn(it) }
      tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
    }
  }
}

tasks.register("verifyAll") {
  group = "verification"
  description = "Runs all checks including native (requires macOS)"
  dependsOn("verify", "verifyNative")
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
