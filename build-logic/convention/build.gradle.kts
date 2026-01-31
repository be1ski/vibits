plugins {
  `kotlin-dsl`
}

java {
  sourceCompatibility = JavaVersion.VERSION_21
  targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
  jvmToolchain(21)
}

dependencies {
  implementation(libs.android.gradle.plugin)
  implementation(libs.compose.multiplatform.plugin)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.compose.plugin)
  implementation(libs.kover.plugin)
  implementation(libs.ksp.plugin)
  implementation(libs.metro.plugin)
  implementation(libs.room.plugin)
}
