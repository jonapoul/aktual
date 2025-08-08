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

plugins {
  // https://github.com/JetBrains/compose-hot-reload?tab=readme-ov-file#set-up-automatic-provisioning-of-the-jetbrains-runtime-jbr-via-gradle
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

private fun module(path: String) = include(":modules:$path")

include(":app:android")
include(":app:desktop")
include(":app:di")
include(":app:nav")
module("about:data")
module("about:ui")
module("about:vm")
module("account:domain")
module("account:model")
module("account:ui")
module("account:vm")
module("api:actual")
module("api:builder")
module("api:github")
module("budget:data")
module("budget:di")
module("budget:encryption")
module("budget:list:ui")
module("budget:list:vm")
module("budget:model")
module("budget:reports:ui")
module("budget:reports:vm")
module("budget:sync:ui")
module("budget:sync:vm")
module("budget:transactions:ui")
module("budget:transactions:vm")
module("codegen:annotation")
module("codegen:ksp")
module("core:connection")
module("core:di")
module("core:model")
module("core:ui")
module("l10n")
module("logging")
module("prefs")
module("settings:ui")
module("settings:vm")
module("test:android")
module("test:api")
module("test:compose")
module("test:kotlin")
