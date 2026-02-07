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
        implementation(projects.feature.auth.domain)
        implementation(projects.feature.memos.domain)
        implementation(projects.feature.mode.domain)
        implementation(projects.feature.settings.domain)
        implementation(libs.compose.resources)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material3)
        implementation(libs.compose.material.icons.extended)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
        implementation(libs.kotlinx.coroutines.core)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.elm.test)
        implementation(projects.feature.auth.domain.testing)
        implementation(projects.feature.memos.domain.testing)
        implementation(projects.feature.mode.domain.testing)
        implementation(projects.feature.onboarding.domain)
        implementation(projects.feature.onboarding.domain.testing)
        implementation(projects.feature.settings.domain.testing)
        implementation(kotlin("test"))
        implementation(libs.kotlinx.coroutines.test)
      }
    }
    val desktopTest by getting {
      dependencies {
        implementation(projects.core.ui.testing)
      }
    }
  }
}
