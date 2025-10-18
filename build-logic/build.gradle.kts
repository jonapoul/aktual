import java.util.Properties

plugins {
  `kotlin-dsl`
  idea
}

idea {
  module {
    isDownloadSources = true
    isDownloadJavadoc = true
  }
}

// Pull java version property from project's root properties file, since build-logic doesn't have access to it
val props = Properties().also { it.load(file("../gradle.properties").reader()) }
val javaInt = props["blueprint.javaVersion"]?.toString()?.toInt() ?: error("Failed getting java version from $props")
val javaVersion = JavaVersion.toVersion(javaInt)

java {
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

kotlin {
  jvmToolchain(javaInt)
}

dependencies {
  fun compileOnly(plugin: Provider<PluginDependency>) =
    with(plugin.get()) { compileOnly("$pluginId:$pluginId.gradle.plugin:$version") }

  compileOnly(libs.plugins.agp.app)
  compileOnly(libs.plugins.agp.lib)
  compileOnly(libs.plugins.androidCacheFix)
  compileOnly(libs.plugins.burst)
  compileOnly(libs.plugins.compose)
  compileOnly(libs.plugins.composeHotReload)
  compileOnly(libs.plugins.detekt)
  compileOnly(libs.plugins.kotlin.android)
  compileOnly(libs.plugins.kotlin.compose)
  compileOnly(libs.plugins.kotlin.jvm)
  compileOnly(libs.plugins.kotlin.multiplatform)
  compileOnly(libs.plugins.kotlin.serialization)
  compileOnly(libs.plugins.kotlin.powerAssert)
  compileOnly(libs.plugins.kover)
  compileOnly(libs.plugins.ksp)
  compileOnly(libs.plugins.licensee)
  compileOnly(libs.plugins.metro)
  compileOnly(libs.plugins.spotless)

  implementation(libs.okio)
  implementation(libs.blueprint.core)
  implementation(libs.blueprint.recipes)
}

tasks.validatePlugins {
  enableStricterValidation = true
  failOnWarning = true
}

gradlePlugin {
  plugins {
    operator fun String.invoke(impl: String) = register(this) {
      id = this@invoke
      implementationClass = impl
    }

    "actual.convention.android.base"(impl = "actual.gradle.ConventionAndroidBase")
    "actual.convention.android.library"(impl = "actual.gradle.ConventionAndroidLibrary")
    "actual.convention.compose"(impl = "actual.gradle.ConventionCompose")
    "actual.convention.detekt"(impl = "actual.gradle.ConventionDetekt")
    "actual.convention.idea"(impl = "actual.gradle.ConventionIdea")
    "actual.convention.kotlin.base"(impl = "actual.gradle.ConventionKotlinBase")
    "actual.convention.kotlin.jvm"(impl = "actual.gradle.ConventionKotlinJvm")
    "actual.convention.kover"(impl = "actual.gradle.ConventionKover")
    "actual.convention.licensee"(impl = "actual.gradle.ConventionLicensee")
    "actual.convention.di"(impl = "actual.gradle.ConventionDi")
    "actual.convention.style"(impl = "actual.gradle.ConventionStyle")
    "actual.convention.spotless"(impl = "actual.gradle.ConventionSpotless")
    "actual.convention.test"(impl = "actual.gradle.ConventionTest")

    "actual.module.android"(impl = "actual.gradle.ModuleAndroid")
    "actual.module.compose"(impl = "actual.gradle.ModuleCompose")
    "actual.module.di"(impl = "actual.gradle.ModuleDi")
    "actual.module.jvm"(impl = "actual.gradle.ModuleJvm")
    "actual.module.multiplatform"(impl = "actual.gradle.ModuleMultiplatform")
    "actual.module.viewmodel"(impl = "actual.gradle.ModuleViewModel")
  }
}
