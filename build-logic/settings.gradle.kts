@file:Suppress("UnstableApiUsage")

rootProject.name = "build-logic"

apply(from = "../gradle/repositories.gradle.kts")

dependencyResolutionManagement {
  versionCatalogs {
    create("libs") {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
