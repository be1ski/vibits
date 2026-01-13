import org.gradle.api.tasks.JavaExec

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
    mainClass = "space.be1ski.memos.desktop.DesktopMainKt"
    nativeDistributions {
      targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg)
      packageName = "Memos"
      packageVersion = "1.0.0"
    }
  }
}

tasks.matching { it.name == "run" || it.name == "desktopRun" }.configureEach {
  doFirst {
    (this as? JavaExec)?.systemProperty("memos.env", "dev")
  }
}
