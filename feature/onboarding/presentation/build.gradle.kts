plugins {
  id("vibits.kmp.compose")
  id("vibits.kmp.metro")
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.elm)
        implementation(projects.core.platform)
        implementation(projects.core.strings)
        implementation(projects.core.ui)
        implementation(projects.core.utils)
        implementation(projects.feature.habits.domain)
        implementation(projects.feature.habits.presentation)
        implementation(projects.feature.onboarding.domain)
        implementation(libs.compose.resources)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.material.icons.extended)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.elm.test)
        implementation(projects.feature.memos.data)
        implementation(projects.feature.memos.data.testing)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.onboarding.data)
        implementation(projects.feature.onboarding.domain.testing)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
  }
}
