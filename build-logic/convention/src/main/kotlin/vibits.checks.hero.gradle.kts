import com.github.gradle.node.NodeExtension
import com.github.gradle.node.npm.task.NpmTask
import com.github.gradle.node.task.NodeTask

plugins {
  id("com.github.node-gradle.node")
}

val heroDir = rootProject.file(".github/hero")

configure<NodeExtension> {
  version = "24.0.0"
  download = true
  nodeProjectDir = heroDir
}

val heroNpmInstall = tasks.register<NpmTask>("heroNpmInstall") {
  group = "hero"
  description = "Install Node dependencies for hero image renderer"
  workingDir = heroDir
  args = listOf("install")
  inputs.file(heroDir.resolve("package.json"))
  outputs.dir(heroDir.resolve("node_modules"))
}

val screenshotsDir = heroDir.resolve("ui-screenshots")

val copyScreenshotsForHero = tasks.register<Copy>("copyScreenshotsForHero") {
  group = "hero"
  description = "Copy screenshot test output into hero renderer directory"
  dependsOn("screenshotTests")
  from(rootProject.layout.buildDirectory.dir("ui-screenshots"))
  into(screenshotsDir)
}

val renderHeroImage = tasks.register<NodeTask>("renderHeroImage") {
  group = "hero"
  description = "Render hero image using Puppeteer"
  dependsOn(heroNpmInstall, copyScreenshotsForHero)
  script = heroDir.resolve("render.mjs")
  inputs.dir(screenshotsDir)
  inputs.file(heroDir.resolve("config.json"))
  inputs.file(heroDir.resolve("render.mjs"))
  outputs.file(heroDir.resolve("hero.webp"))
}

tasks.register("generateHeroImage") {
  group = "hero"
  description = "Generate hero.webp from screenshot tests"
  notCompatibleWithConfigurationCache("copies files at execution time")
  dependsOn(renderHeroImage)

  val heroOutput = heroDir.resolve("hero.webp")
  val buildHeroDir = rootProject.layout.buildDirectory.dir("hero").get().asFile

  doLast {
    buildHeroDir.mkdirs()
    heroOutput.copyTo(buildHeroDir.resolve("hero.webp"), overwrite = true)
  }
}
