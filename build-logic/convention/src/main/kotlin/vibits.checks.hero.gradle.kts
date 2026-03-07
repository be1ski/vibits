val buildHeroDir = rootProject.layout.buildDirectory.dir("hero")

tasks.register("generateHeroImage") {
  group = "hero"
  description = "Generate hero.webp from screenshot tests"
  dependsOn("screenshotTests")

  subprojects {
    tasks.findByName("heroDesktopTest")?.let { dependsOn(it) }
  }

  notCompatibleWithConfigurationCache("reads files at execution time")

  doLast {
    val heroPng = subprojects
      .map { File(it.layout.buildDirectory.get().asFile, "hero/hero.png") }
      .firstOrNull { it.exists() }
    requireNotNull(heroPng) { "hero.png not found in any subproject build directory" }

    val outputDir = buildHeroDir.get().asFile
    outputDir.mkdirs()
    val heroWebp = File(outputDir, "hero.webp")

    val cwebpSuccess = try {
      val process = ProcessBuilder(
        "cwebp", "-q", "90", "-alpha_q", "100",
        heroPng.absolutePath, "-o", heroWebp.absolutePath,
      ).redirectErrorStream(true).start()
      process.waitFor() == 0
    } catch (_: Exception) {
      false
    }

    if (cwebpSuccess) {
      println("Done: ${heroWebp.name}")
    } else {
      heroPng.copyTo(File(outputDir, "hero.png"), overwrite = true)
      println("Warning: cwebp not found, saved as hero.png")
    }
  }
}
