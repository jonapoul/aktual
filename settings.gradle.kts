@file:Suppress("UnstableApiUsage", "HasPlatformType")

rootProject.name = "actual-android"

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    mavenLocal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupByRegex(".*android.*")
        includeGroupByRegex(".*google.*")
      }
    }
    mavenCentral()
    maven("https://jitpack.io")
    mavenLocal()
  }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

private fun module(path: String) = include(":modules:$path")

include(":app")
module("about:data")
module("about:res")
module("about:ui")
module("about:vm")
module("account:domain")
module("account:model")
module("account:res")
module("account:ui")
module("account:vm")
module("api:actual")
module("api:builder")
module("api:github")
module("budget:data")
module("budget:di")
module("budget:encryption")
module("budget:list:res")
module("budget:list:ui")
module("budget:list:vm")
module("budget:model")
module("budget:sync:res")
module("budget:sync:ui")
module("budget:sync:vm")
module("budget:transactions:res")
module("budget:transactions:ui")
module("budget:transactions:vm")
module("codegen:annotation")
module("codegen:ksp")
module("core:connection")
module("core:di")
module("core:model")
module("core:res")
module("core:ui")
module("logging")
module("prefs")
module("settings:res")
module("settings:ui")
module("settings:vm")
module("test:android")
module("test:compose")
module("test:kotlin")
