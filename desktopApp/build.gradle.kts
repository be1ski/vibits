import org.gradle.api.tasks.JavaExec

val appVersion: String = providers.gradleProperty("appVersion").getOrElse("1.0.0")

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.kotlin.compose)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(libs.koin.core)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(project(":shared"))
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "space.be1ski.vibits.desktop.DesktopMainKt"
    nativeDistributions {
      targetFormats(
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
        org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi
      )
      packageName = "Vibits"
      packageVersion = appVersion

      macOS {
        iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
      }

      windows {
        menuGroup = "Vibits"
        upgradeUuid = "18159995-d967-4CD2-8885-77BFB7EE7B8B"
        iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
      }
    }
  }
}

tasks.matching { it.name == "run" || it.name == "desktopRun" }.configureEach {
  doFirst {
    (this as? JavaExec)?.systemProperty("vibits.env", "dev")
  }
}
