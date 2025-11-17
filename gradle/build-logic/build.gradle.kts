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
val props = Properties().also { it.load(file("../../gradle.properties").reader()) }
val javaInt = props["aktual.javaVersion"]?.toString()?.toInt() ?: error("Failed getting java version from $props")
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

    "aktual.convention.android.base"(impl = "aktual.gradle.ConventionAndroidBase")
    "aktual.convention.compose"(impl = "aktual.gradle.ConventionCompose")
    "aktual.convention.detekt"(impl = "aktual.gradle.ConventionDetekt")
    "aktual.convention.idea"(impl = "aktual.gradle.ConventionIdea")
    "aktual.convention.kotlin.base"(impl = "aktual.gradle.ConventionKotlinBase")
    "aktual.convention.kotlin.jvm"(impl = "aktual.gradle.ConventionKotlinJvm")
    "aktual.convention.kover"(impl = "aktual.gradle.ConventionKover")
    "aktual.convention.licensee"(impl = "aktual.gradle.ConventionLicensee")
    "aktual.convention.style"(impl = "aktual.gradle.ConventionStyle")
    "aktual.convention.spotless"(impl = "aktual.gradle.ConventionSpotless")
    "aktual.convention.test"(impl = "aktual.gradle.ConventionTest")

    "aktual.module.android"(impl = "aktual.gradle.ModuleAndroid")
    "aktual.module.compose"(impl = "aktual.gradle.ModuleCompose")
    "aktual.module.di"(impl = "aktual.gradle.ModuleDi")
    "aktual.module.jvm"(impl = "aktual.gradle.ModuleJvm")
    "aktual.module.multiplatform"(impl = "aktual.gradle.ModuleMultiplatform")
    "aktual.module.viewmodel"(impl = "aktual.gradle.ModuleViewModel")
  }
}
