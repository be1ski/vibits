plugins {
  id("vibits.kmp.compose")
  id("vibits.kmp.metro")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  androidLibrary {
    androidResources {
      enable = true
    }
  }

  sourceSets {
    commonMain {
      dependencies {
        api(projects.core.elm)
        api(projects.core.platform)
        api(projects.core.strings)
        api(projects.core.ui)
        api(projects.core.utils)
        api(projects.feature.auth.data)
        api(projects.feature.auth.domain)
        api(projects.feature.habits.domain)
        api(projects.feature.habits.presentation)
        api(projects.feature.memos.data)
        api(projects.feature.memos.domain)
        api(projects.feature.memos.presentation)
        api(projects.feature.mode.data)
        api(projects.feature.mode.domain)
        api(projects.feature.mode.presentation)
        api(projects.feature.onboarding.data)
        api(projects.feature.onboarding.domain)
        api(projects.feature.onboarding.presentation)
        api(projects.feature.settings.data)
        api(projects.feature.settings.domain)
        api(projects.feature.settings.presentation)
        api(projects.feature.sync.data)
        api(projects.feature.sync.domain)
        implementation(libs.compose.foundation)
        implementation(libs.compose.material)
        implementation(libs.compose.material.icons.extended)
        implementation(libs.compose.material3)
        implementation(libs.compose.resources)
        implementation(libs.compose.runtime)
        implementation(libs.compose.ui)
        implementation(libs.kotlinx.atomicfu)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.datetime)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.serialization.kotlinx.json)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.core.elm.test)
        implementation(kotlin("test"))
        implementation(libs.ktor.client.mock)
        implementation(libs.kotlinx.coroutines.test)
      }
    }
    androidMain {
      dependencies {
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.core.ktx)
        implementation(libs.ktor.client.okhttp)
      }
    }
    desktopMain {
      dependencies {
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.ktor.client.cio)
        implementation(libs.slf4j.simple)
      }
    }
    iosMain {
      dependencies {
        implementation(libs.ktor.client.darwin)
      }
    }
    wasmJsMain {
      dependencies {
        implementation(libs.ktor.client.js)
      }
    }
  }
}
