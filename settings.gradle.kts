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
module("account:login:domain")
module("account:login:res")
module("account:login:ui")
module("account:login:vm")
module("account:model")
module("account:password:domain")
module("account:password:res")
module("account:password:ui")
module("account:password:vm")
module("api:actual")
module("api:builder")
module("api:github")
module("budget:db")
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
module("test:buildconfig")
module("test:compose")
module("test:coroutines")
module("test:db")
module("test:files")
module("test:http")
module("test:json")
module("test:prefs")
module("url:res")
module("url:ui")
module("url:vm")
