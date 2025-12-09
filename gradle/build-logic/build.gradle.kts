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
  fun compileOnlyPlugin(plugin: Provider<PluginDependency>) =
    compileOnly(plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })

  compileOnlyPlugin(libs.plugins.agp.app)
  compileOnlyPlugin(libs.plugins.agp.lib)
  compileOnlyPlugin(libs.plugins.androidCacheFix)
  compileOnlyPlugin(libs.plugins.buildconfig)
  compileOnlyPlugin(libs.plugins.burst)
  compileOnlyPlugin(libs.plugins.compose)
  compileOnlyPlugin(libs.plugins.detekt)
  compileOnlyPlugin(libs.plugins.kotlin.compose)
  compileOnlyPlugin(libs.plugins.kotlin.jvm)
  compileOnlyPlugin(libs.plugins.kotlin.multiplatform)
  compileOnlyPlugin(libs.plugins.kotlin.serialization)
  compileOnlyPlugin(libs.plugins.kover)
  compileOnlyPlugin(libs.plugins.ksp)
  compileOnlyPlugin(libs.plugins.licensee)
  compileOnlyPlugin(libs.plugins.metro)

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
    "aktual.convention.buildconfig"(impl = "aktual.gradle.ConventionBuildConfig")
    "aktual.convention.compose"(impl = "aktual.gradle.ConventionCompose")
    "aktual.convention.detekt"(impl = "aktual.gradle.ConventionDetekt")
    "aktual.convention.idea"(impl = "aktual.gradle.ConventionIdea")
    "aktual.convention.kotlin.base"(impl = "aktual.gradle.ConventionKotlinBase")
    "aktual.convention.kotlin.jvm"(impl = "aktual.gradle.ConventionKotlinJvm")
    "aktual.convention.kover"(impl = "aktual.gradle.ConventionKover")
    "aktual.convention.licensee"(impl = "aktual.gradle.ConventionLicensee")
    "aktual.convention.style"(impl = "aktual.gradle.ConventionStyle")
    "aktual.convention.test"(impl = "aktual.gradle.ConventionTest")

    "aktual.module.android"(impl = "aktual.gradle.ModuleAndroid")
    "aktual.module.compose"(impl = "aktual.gradle.ModuleCompose")
    "aktual.module.di"(impl = "aktual.gradle.ModuleDi")
    "aktual.module.jvm"(impl = "aktual.gradle.ModuleJvm")
    "aktual.module.multiplatform"(impl = "aktual.gradle.ModuleMultiplatform")
    "aktual.module.viewmodel"(impl = "aktual.gradle.ModuleViewModel")
  }
}
