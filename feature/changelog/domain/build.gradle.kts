plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.utils)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.changelog.data)
        implementation(projects.feature.changelog.domain.testing)
      }
    }
  }
}
