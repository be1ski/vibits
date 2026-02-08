val outputDir: File = rootProject.layout.buildDirectory.dir("ui-screenshots").get().asFile

// Screenshot PNGs are side-effects of desktopTest not declared as task outputs,
// so Gradle's build cache doesn't restore them. Force re-execution when collecting screenshots.
if (gradle.startParameter.taskNames.any { it.contains("screenshotTests") }) {
  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.matching { it.name == "desktopTest" }.configureEach {
        outputs.cacheIf { false }
        outputs.upToDateWhen { false }
      }
    }
  }
}

tasks.register("screenshotTests") {
  group = "verification"
  description = "Runs screenshot tests and collects screenshots into build/ui-screenshots"
  notCompatibleWithConfigurationCache("walks subproject build directories at execution time")

  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("desktopTest")?.let { dependsOn(it) }
    }
  }

  doLast {
    outputDir.deleteRecursively()
    outputDir.mkdirs()
    subprojects.forEach { subproject ->
      val dir = subproject.layout.buildDirectory.dir("ui-screenshots").get().asFile
      if (dir.exists()) {
        dir.walkTopDown().filter { it.isFile && it.extension == "png" }.forEach { file ->
          file.copyTo(File(outputDir, file.name), overwrite = true)
        }
      }
    }
  }
}
