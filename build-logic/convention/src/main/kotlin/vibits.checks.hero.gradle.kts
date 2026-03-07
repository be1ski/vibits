import hero.HeroImageRenderer

val screenshotsDir = rootProject.layout.buildDirectory.dir("ui-screenshots")
val buildHeroDir = rootProject.layout.buildDirectory.dir("hero")

tasks.register("generateHeroImage") {
  group = "hero"
  description = "Generate hero.webp from screenshot tests"
  dependsOn("screenshotTests")

  inputs.dir(screenshotsDir)
  outputs.file(buildHeroDir.map { it.file("hero.webp") })

  notCompatibleWithConfigurationCache("reads files at execution time")

  doLast {
    val renderer = HeroImageRenderer(
      screenshotsDir = screenshotsDir.get().asFile,
      outputFile = buildHeroDir.get().asFile.resolve("hero.webp"),
    )
    renderer.render()
  }
}
