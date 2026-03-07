val outputDir: File = rootProject.layout.buildDirectory.dir("ui-screenshots").get().asFile
val heroDir: File = rootProject.layout.buildDirectory.dir("hero").get().asFile

// Screenshot PNGs are side-effects of desktopTest not declared as task outputs,
// so Gradle's build cache doesn't restore them. Disable caching unconditionally
// because CI always collects screenshots for visual regression testing.
subprojects {
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    tasks.matching { it.name == "desktopTest" }.configureEach {
      outputs.cacheIf { false }
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
    heroDir.mkdirs()
    val heroCandidates = mutableSetOf<String>()
    subprojects.forEach { subproject ->
      val dir = subproject.layout.buildDirectory.dir("ui-screenshots").get().asFile
      if (dir.exists()) {
        dir.listFiles().orEmpty().filter { it.isFile && it.extension == "png" }.forEach { file ->
          file.copyTo(File(outputDir, file.name), overwrite = true)
        }
      }
      val manifest = File(subproject.layout.buildDirectory.dir("hero").get().asFile, "hero-candidates.txt")
      if (manifest.exists()) {
        heroCandidates += manifest.readLines().filter { it.isNotBlank() }
      }
    }
    if (heroCandidates.isNotEmpty()) {
      File(heroDir, "hero-candidates.txt").writeText(heroCandidates.sorted().joinToString("\n") + "\n")
    }
  }
}
