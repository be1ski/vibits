plugins {
  id("vibits.kmp.compose")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(libs.compose.resources)
        implementation(libs.compose.runtime)
      }
    }
  }
}

compose.resources {
  packageOfResClass = "space.be1ski.vibits.core.strings.generated"
  publicResClass = true
}
