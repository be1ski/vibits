dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
  versionCatalogs {
    create("buildConfig") {
      from(files("../gradle/buildConfig.versions.toml"))
    }
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "build-logic"
include(":convention")
