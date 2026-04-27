plugins {
  id("org.jetbrains.kotlin.multiplatform")
  id("dev.zacsweers.metro")
}

metro {
  generateContributionProviders.set(true)
}
