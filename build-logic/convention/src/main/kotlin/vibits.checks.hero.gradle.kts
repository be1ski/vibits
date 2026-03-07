val buildHeroDir = rootProject.layout.buildDirectory.dir("hero")

tasks.register("generateHeroImage") {
  group = "hero"
  description = "Generate hero.webp variants from screenshot tests"
  dependsOn("screenshotTests")

  subprojects {
    tasks.findByName("heroDesktopTest")?.let { dependsOn(it) }
  }

  notCompatibleWithConfigurationCache("reads files at execution time")

  doLast {
    val heroPngs = subprojects
      .flatMap { sub ->
        val heroDir = File(sub.layout.buildDirectory.get().asFile, "hero")
        heroDir.listFiles()?.filter { it.name.startsWith("hero-") && it.extension == "png" }
          ?: emptyList()
      }
    require(heroPngs.isNotEmpty()) { "No hero-*.png files found in any subproject build directory" }

    val outputDir = buildHeroDir.get().asFile
    outputDir.mkdirs()

    for (heroPng in heroPngs) {
      val baseName = heroPng.nameWithoutExtension
      val heroWebp = File(outputDir, "$baseName.webp")

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
        heroPng.copyTo(File(outputDir, heroPng.name), overwrite = true)
        println("Warning: cwebp not found, saved as ${heroPng.name}")
      }
    }
  }
}
