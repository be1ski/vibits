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
}

rootProject.name = "Vibits"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
  ":androidApp",
  ":desktopApp",
  ":iosApp:framework",
  ":webApp",

  ":feature:main",

  ":core:platform",
  ":core:elm",
  ":core:elm:test",
  ":core:ui",
  ":core:strings",

  ":feature:auth:domain",
  ":feature:auth:data",

  ":feature:memos:domain",
  ":feature:memos:data",
  ":feature:memos:presentation",

  ":feature:habits:domain",
  ":feature:habits:presentation",

  ":feature:mode:domain",
  ":feature:mode:data",
  ":feature:mode:presentation",

  ":feature:settings:domain",
  ":feature:settings:data",
  ":feature:settings:presentation",

  ":feature:onboarding:domain",
  ":feature:onboarding:data",
  ":feature:onboarding:presentation",

  ":feature:sync:domain",
  ":feature:sync:data",
)
