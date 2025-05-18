@file:Suppress("UnstableApiUsage", "HasPlatformType")

import kotlin.text.replace

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

val modulesDir = rootProject.projectDir.resolve("modules")

private fun Settings.includeModule(path: String) {
  include(path)
  project(":$path").projectDir = modulesDir.resolve(path.replace(':', File.separatorChar))
}

include(":app")
includeModule("about:info:data")
includeModule("about:info:res")
includeModule("about:info:ui")
includeModule("about:info:vm")
includeModule("about:licenses:data")
includeModule("about:licenses:res")
includeModule("about:licenses:ui")
includeModule("about:licenses:vm")
includeModule("account:login:domain")
includeModule("account:login:res")
includeModule("account:login:ui")
includeModule("account:login:vm")
includeModule("account:model")
includeModule("account:password:domain")
includeModule("account:password:res")
includeModule("account:password:ui")
includeModule("account:password:vm")
includeModule("api:actual")
includeModule("api:builder")
includeModule("api:di")
includeModule("api:github")
includeModule("budget:list:res")
includeModule("budget:list:ui")
includeModule("budget:list:vm")
includeModule("budget:model")
includeModule("budget:sync:res")
includeModule("budget:sync:ui")
includeModule("budget:sync:vm")
includeModule("codegen:annotation")
includeModule("codegen:ksp")
includeModule("core:connection")
includeModule("core:di")
includeModule("core:files")
includeModule("core:model")
includeModule("core:res")
includeModule("core:ui")
includeModule("db")
includeModule("prefs")
includeModule("settings:res")
includeModule("settings:ui")
includeModule("settings:vm")
includeModule("test:buildconfig")
includeModule("test:compose")
includeModule("test:coroutines")
includeModule("test:db")
includeModule("test:hilt")
includeModule("test:http")
includeModule("test:prefs")
includeModule("url:res")
includeModule("url:ui")
includeModule("url:vm")
