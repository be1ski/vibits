plugins {
  id("vibits.kmp.library")
  id("vibits.kmp.metro")
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  sourceSets {
    commonMain {
      dependencies {
        implementation(projects.core.platform)
        implementation(projects.core.utils)
        implementation(projects.feature.changelog.domain)
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.content.negotiation)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.serialization.kotlinx.json)
      }
    }
    commonTest {
      dependencies {
        implementation(projects.feature.changelog.domain.testing)
        implementation(libs.ktor.client.mock)
      }
    }
  }
}
