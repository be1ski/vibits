plugins {
  id("vibits.kmp.library")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        api(projects.feature.sync.domain)
        implementation(projects.feature.memos.domain)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
  }
}
