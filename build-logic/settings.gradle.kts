@file:Suppress("UnstableApiUsage")

rootProject.name = "build-logic"

apply(from = "../gradle/repositories.gradle.kts")

includeBuild("../compiler-plugin")

dependencyResolutionManagement {
  versionCatalogs { register("libs") { from(files("../gradle/libs.versions.toml")) } }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
