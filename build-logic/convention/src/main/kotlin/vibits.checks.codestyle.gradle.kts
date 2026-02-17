import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension

val libs = the<VersionCatalogsExtension>().named("libs")

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
      version.set(libs.findVersion("ktlintLib").get().toString())
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
      config.setFrom(files("$rootDir/build-logic/config/detekt.yml"))
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
