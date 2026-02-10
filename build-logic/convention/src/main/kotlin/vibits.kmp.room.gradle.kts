plugins {
  id("org.jetbrains.kotlin.multiplatform") apply false
  id("com.google.devtools.ksp")
  id("androidx.room")
}

val libs = the<VersionCatalogsExtension>().named("libs")

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  add("kspAndroid", libs.findLibrary("androidx-room-compiler").get())
  add("kspDesktop", libs.findLibrary("androidx-room-compiler").get())
  add("kspIosArm64", libs.findLibrary("androidx-room-compiler").get())
  add("kspIosSimulatorArm64", libs.findLibrary("androidx-room-compiler").get())
  add("kspIosX64", libs.findLibrary("androidx-room-compiler").get())
}
