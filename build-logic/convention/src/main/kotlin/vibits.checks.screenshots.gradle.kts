tasks.register("screenshotTests") {
  group = "verification"
  description = "Runs screenshot tests and collects screenshots into build/ui-screenshots"

  subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
      tasks.findByName("desktopTest")?.let { dependsOn(it) }
    }
  }

  doLast {
    val outputDir = rootProject.layout.buildDirectory.dir("ui-screenshots").get().asFile
    outputDir.deleteRecursively()
    outputDir.mkdirs()
    rootProject.subprojects.forEach { sub ->
      val screenshotsDir = sub.layout.buildDirectory.dir("ui-screenshots").get().asFile
      if (screenshotsDir.exists()) {
        screenshotsDir.copyRecursively(outputDir, overwrite = true)
      }
    }
  }
}
