pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
  repositories {
    google()
    mavenCentral()
  }
  versionCatalogs {
    create("buildConfig") {
      from(files("gradle/build-config.versions.toml"))
    }
  }
}

rootProject.name = "Vibits"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
  ":app:android",
  ":app:desktop",
  ":app:ios:framework",
  ":app:web",

  ":feature:homescreen",

  ":core:elm",
  ":core:elm:test",
  ":core:platform",
  ":core:platform:testing",
  ":core:strings",
  ":core:ui",
  ":core:ui:testing",
  ":core:utils",

  ":feature:auth:domain",
  ":feature:auth:domain:testing",
  ":feature:auth:data",

  ":feature:memos:domain",
  ":feature:memos:domain:testing",
  ":feature:memos:data",
  ":feature:memos:data:testing",
  ":feature:memos:presentation",

  ":feature:habits:domain",
  ":feature:habits:presentation",

  ":feature:mode:domain",
  ":feature:mode:domain:testing",
  ":feature:mode:data",
  ":feature:mode:presentation",

  ":feature:settings:domain",
  ":feature:settings:domain:testing",
  ":feature:settings:data",
  ":feature:settings:presentation",

  ":feature:onboarding:domain",
  ":feature:onboarding:domain:testing",
  ":feature:onboarding:data",
  ":feature:onboarding:presentation",

  ":feature:sync:domain",
  ":feature:sync:domain:testing",
  ":feature:sync:data",
)
