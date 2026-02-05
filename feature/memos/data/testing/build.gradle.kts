plugins {
  id("vibits.kmp.library")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.feature.memos.data)
      }
    }
  }
}
