plugins {
  id("vibits.kmp.library")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.elm)
        implementation(kotlin("test"))
      }
    }
  }
}
