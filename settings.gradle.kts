rootProject.name = "aktual"

apply(from = "gradle/repositories.gradle.kts")

pluginManagement {
  includeBuild("gradle/build-logic")
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

private fun module(path: String) = include(":aktual-$path")

module("core:model")
