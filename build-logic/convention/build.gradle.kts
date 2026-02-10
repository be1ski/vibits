plugins {
  `kotlin-dsl`
}

val javaVersion = buildConfig.versions.java.get()

java {
  sourceCompatibility = JavaVersion.toVersion(javaVersion)
  targetCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
  jvmToolchain(javaVersion.toInt())
}

dependencies {
  // Workaround to make version catalogs type-safe in convention plugins
  // https://github.com/gradle/gradle/issues/15383
  api(files(buildConfig.javaClass.superclass.protectionDomain.codeSource.location))
  api(files(libs.javaClass.superclass.protectionDomain.codeSource.location))

  implementation(libs.android.gradle.plugin)
  implementation(libs.compose.multiplatform.plugin)
  implementation(libs.detekt.plugin)
  implementation(libs.kotlin.gradle.plugin)
  implementation(libs.kotlin.compose.plugin)
  implementation(libs.kover.plugin)
  implementation(libs.ksp.plugin)
  implementation(libs.ktlint.plugin)
  implementation(libs.metro.plugin)
  implementation(libs.room.plugin)
}
