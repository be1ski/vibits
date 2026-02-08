val screenshotDirs: List<File> = subprojects
  .filter { it.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform") }
  .map { it.layout.buildDirectory.dir("ui-screenshots").get().asFile }

val outputDir: File = rootProject.layout.buildDirectory.dir("ui-screenshots").get().asFile

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
    screenshotDirs.forEach { dir ->
      if (dir.exists()) {
        dir.walkTopDown().filter { it.isFile && it.extension == "png" }.forEach { file ->
          file.copyTo(File(outputDir, file.name), overwrite = true)
        }
      }
    }
  }
}
