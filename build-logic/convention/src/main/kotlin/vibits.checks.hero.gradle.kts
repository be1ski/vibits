val buildHeroDir = rootProject.layout.buildDirectory.dir("hero")
val publishedHeroPngNames = setOf("hero-dark.png", "hero-light.png")

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
        heroDir.listFiles()?.filter { it.extension == "png" && it.name in publishedHeroPngNames }
          ?: emptyList()
      }
    require(heroPngs.isNotEmpty()) {
      "No published hero PNG files found. Expected: ${publishedHeroPngNames.joinToString(", ")}"
    }

    val outputDir = buildHeroDir.get().asFile
    outputDir.mkdirs()
    delete(
      fileTree(outputDir) {
        include("hero-*.webp")
        include("hero-*.png")
      },
    )

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
