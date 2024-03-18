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
include(":modules:api:json")
include(":modules:api:model")

include(":modules:core:connection")
include(":modules:core:model")
include(":modules:core:ui")
include(":modules:core:res")

include(":modules:login:ui")
include(":modules:login:vm")

include(":modules:server-url:prefs")
include(":modules:server-url:ui")
include(":modules:server-url:vm")

include(":modules:nav")

include(":modules:test:android")
