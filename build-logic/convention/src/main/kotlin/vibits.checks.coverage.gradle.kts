plugins {
  id("org.jetbrains.kotlinx.kover")
}

dependencies {
  subprojects.forEach { subproject ->
    subproject.plugins.withId("org.jetbrains.kotlinx.kover") {
      kover(subproject)
    }
  }
}
