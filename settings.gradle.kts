@file:Suppress("UnstableApiUsage")

rootProject.name = "actual-android"

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

include(":app")

include(":modules:api:client")
include(":modules:api:model")

include(":modules:core:theme")

include(":modules:di")
