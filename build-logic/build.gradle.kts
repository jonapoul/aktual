import java.util.Properties

plugins {
  `kotlin-dsl`
}

// Pull java version property from project's root properties file, since build-logic doesn't have access to it
val propsFile = file("../gradle.properties")
if (!propsFile.exists()) error("No file found at ${propsFile.absolutePath}")
val props = Properties().also { it.load(propsFile.reader()) }
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
  compileOnly(libs.plugin.agp)
  compileOnly(libs.plugin.androidCacheFix)
  compileOnly(libs.plugin.catalog)
  compileOnly(libs.plugin.compose)
  compileOnly(libs.plugin.dependencyAnalysis)
  compileOnly(libs.plugin.dependencyGraph)
  compileOnly(libs.plugin.dependencySort)
  compileOnly(libs.plugin.detekt)
  compileOnly(libs.plugin.hilt)
  compileOnly(libs.plugin.kotlin.gradle)
  compileOnly(libs.plugin.kotlin.powerAssert)
  compileOnly(libs.plugin.kover)
  compileOnly(libs.plugin.ksp)
  compileOnly(libs.plugin.ktlint)
  compileOnly(libs.plugin.licensee)
  implementation(libs.plugin.blueprint.core)
  implementation(libs.plugin.blueprint.recipes)
}

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
}

gradlePlugin {
  plugins {
    create(id = "actual.convention.android.base", impl = "actual.gradle.ConventionAndroidBase")
    create(id = "actual.convention.android.library", impl = "actual.gradle.ConventionAndroidLibrary")
    create(id = "actual.convention.compose", impl = "actual.gradle.ConventionCompose")
    create(id = "actual.convention.detekt", impl = "actual.gradle.ConventionDetekt")
    create(id = "actual.convention.diagrams", impl = "actual.gradle.ConventionDiagrams")
    create(id = "actual.convention.hilt", impl = "actual.gradle.ConventionHilt")
    create(id = "actual.convention.kotlin.base", impl = "actual.gradle.ConventionKotlinBase")
    create(id = "actual.convention.kotlin.jvm", impl = "actual.gradle.ConventionKotlinJvm")
    create(id = "actual.convention.kover", impl = "actual.gradle.ConventionKover")
    create(id = "actual.convention.ktlint", impl = "actual.gradle.ConventionKtlint")
    create(id = "actual.convention.idea", impl = "actual.gradle.ConventionIdea")
    create(id = "actual.convention.licensee", impl = "actual.gradle.ConventionLicensee")
    create(id = "actual.convention.sortdependencies", impl = "actual.gradle.ConventionSortDependencies")
    create(id = "actual.convention.style", impl = "actual.gradle.ConventionStyle")
    create(id = "actual.convention.test", impl = "actual.gradle.ConventionTest")
    create(id = "actual.module.android", impl = "actual.gradle.ModuleAndroid")
    create(id = "actual.module.compose", impl = "actual.gradle.ModuleCompose")
    create(id = "actual.module.hilt", impl = "actual.gradle.ModuleHilt")
    create(id = "actual.module.jvm", impl = "actual.gradle.ModuleJvm")
    create(id = "actual.module.multiplatform", impl = "actual.gradle.ModuleMultiplatform")
    create(id = "actual.module.navigation", impl = "actual.gradle.ModuleNavigation")
    create(id = "actual.module.resources", impl = "actual.gradle.ModuleResources")
    create(id = "actual.module.viewmodel", impl = "actual.gradle.ModuleViewModel")
  }
}

fun NamedDomainObjectContainer<PluginDeclaration>.create(id: String, impl: String) = create(id) {
  this.id = id
  implementationClass = impl
}
