plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.firebase.appdistribution) apply false
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.ktlint) apply false
  id("vibits.checks.codestyle")
  id("vibits.checks.structure")
  id("vibits.checks.coverage")
  id("vibits.checks.githooks")
  id("vibits.checks.screenshots")
  id("vibits.checks.hero")
  id("vibits.checks.verification")
}
