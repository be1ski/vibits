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

tasks.register("checkConventions") {
  group = "verification"
  description = "Checks architecture conventions across source files"
  doLast {
    val violations = mutableListOf<String>()
    val sourceSets = listOf("commonMain", "androidMain", "desktopMain", "iosMain", "wasmJsMain")

    subprojects.forEach { sub ->
      sourceSets.forEach { sourceSet ->
        val srcDir = sub.file("src/$sourceSet/kotlin")
        if (!srcDir.exists()) return@forEach
        srcDir.walkTopDown().filter { it.extension == "kt" }.forEach { file ->
          val relativePath = file.relativeTo(rootDir).path
          val pkg = file.readLines().firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim() ?: ""
          val content = file.readText()
          val inUiOrView = pkg.contains(".ui.") || pkg.contains(".view.") || pkg.endsWith(".ui") || pkg.endsWith(".view")
          val inPlatformOrRoom = pkg.contains(".platform.") || pkg.contains(".room.") || pkg.endsWith(".platform") || pkg.endsWith(".room")
          val inTest = pkg.contains(".test.") || pkg.contains(".testing.") || pkg.endsWith(".test") || pkg.endsWith(".testing")

          if (!inUiOrView && content.contains("@Composable")) {
            violations += "$relativePath: @Composable found outside ui/view package ($pkg)"
          }
          if (!inPlatformOrRoom && content.contains(Regex("\\bexpect\\s+(fun|class|interface|object|val|var|abstract|annotation)\\b"))) {
            violations += "$relativePath: expect declaration found outside platform/room package ($pkg)"
          }
          if (sourceSet == "commonMain" && !inTest && file.name.startsWith("Fake")) {
            violations += "$relativePath: Fake class in production code, should be in testing/ module ($pkg)"
          }
        }
      }
    }

    if (violations.isNotEmpty()) {
      violations.forEach { logger.error(it) }
      throw GradleException("Found ${violations.size} convention violation(s):\n${violations.joinToString("\n")}")
    } else {
      logger.lifecycle("Convention check passed — no violations found.")
    }
  }
}

tasks.register("checkJvm") {
  group = "verification"
  description = "Runs JVM checks: detekt, compile, and tests (Linux-safe)"
  dependsOn("checkConventions")
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

tasks.register("checkIos") {
  group = "verification"
  description = "Runs iOS checks: compile, ktlint (requires macOS)"
  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("compileKotlinIosArm64")?.let { dependsOn(it) }
      tasks.findByName("ktlintCheck")?.let { dependsOn(it) }
    }
  }
}

tasks.register("checkAll") {
  group = "verification"
  description = "Runs all checks including iOS (requires macOS)"
  dependsOn("checkJvm", "checkIos")
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
