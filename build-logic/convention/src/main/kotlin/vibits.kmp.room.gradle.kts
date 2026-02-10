plugins {
  id("org.jetbrains.kotlin.multiplatform") apply false
  id("com.google.devtools.ksp")
  id("androidx.room")
}

val libs = the<org.gradle.accessors.dm.LibrariesForLibs>()

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  val roomCompiler = libs.androidx.room.compiler
  listOf("kspAndroid", "kspDesktop", "kspIosArm64", "kspIosSimulatorArm64", "kspIosX64").forEach {
    add(it, roomCompiler)
  }
}
