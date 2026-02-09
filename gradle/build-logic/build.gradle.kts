import dev.detekt.gradle.Detekt

plugins {
  `kotlin-dsl`
  alias(libs.plugins.detekt)
}

val javaFile = layout.projectDirectory.file("../../.java-version")

val jdkVersion = providers
  .fileContents(javaFile)
  .asText
  .map { it.trim().toInt() }

kotlin {
  jvmToolchain(jdkVersion.get())
}

detekt {
  config.from(file("../../config/detekt.yml"))
  source.from("**.kts", "**.kt")
  buildUponDefaultConfig = true
}

val detektCheck by tasks.registering { dependsOn(tasks.withType(Detekt::class)) }
tasks.check { dependsOn(detektCheck) }

dependencies {
  fun compileOnlyPlugin(plugin: Provider<PluginDependency>) =
    compileOnly(plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}" })

  compileOnlyPlugin(libs.plugins.agp.app)
  compileOnlyPlugin(libs.plugins.agp.lib)
  compileOnlyPlugin(libs.plugins.androidCacheFix)
  compileOnlyPlugin(libs.plugins.blueprint)
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

    "aktual.convention.android"(impl = "aktual.gradle.ConventionAndroidBase")
    "aktual.convention.compose"(impl = "aktual.gradle.ConventionCompose")
    "aktual.convention.idea"(impl = "aktual.gradle.ConventionIdea")
    "aktual.convention.kotlin"(impl = "aktual.gradle.ConventionKotlinJvm")
    "aktual.convention.kover"(impl = "aktual.gradle.ConventionKover")
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
