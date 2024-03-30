@file:Suppress("UnstableApiUsage")

rootProject.name = "actual-android"

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
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

include(":modules:budget:list:ui")
include(":modules:budget:list:vm")

include(":modules:core:connection")
include(":modules:core:coroutines")
include(":modules:core:icons")
include(":modules:core:model")
include(":modules:core:prefs")
include(":modules:core:state")
include(":modules:core:ui")
include(":modules:core:res")

include(":modules:login:prefs")
include(":modules:login:ui")
include(":modules:login:vm")

include(":modules:server-url:prefs")
include(":modules:server-url:ui")
include(":modules:server-url:vm")

include(":modules:nav")

include(":modules:test:android")
include(":modules:test:http")
