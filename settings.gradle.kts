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

module("about:data")
module("about:ui")
module("about:vm")
module("account:domain")
module("account:ui")
module("account:vm")
module("api:actual")
module("api:builder")
module("api:github")
module("app:android")
module("app:desktop")
module("app:di")
module("app:nav")
module("budget:data")
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
module("test:di")
module("test:kotlin")
