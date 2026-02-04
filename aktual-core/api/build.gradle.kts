@file:Suppress("UnstableApiUsage")

import aktual.gradle.kspAllConfigs
import blueprint.core.commonMainDependencies
import org.gradle.internal.extensions.stdlib.capitalized
import com.github.gmazzo.buildconfig.BuildConfigSourceSet

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.redacted)
  alias(libs.plugins.ksp)
  alias(libs.plugins.convention.buildconfig)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(project(":aktual-budget:data"))
    api(project(":aktual-core:model"))
    implementation(libs.ktor.auth)
    implementation(libs.ktor.cio)
    implementation(libs.ktor.contentNegotiation)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.serialization.json)
    implementation(libs.preferences.core)
    implementation(project(":aktual-codegen:annotation"))
    implementation(project(":aktual-core:logging"))
    implementation(project(":aktual-core:prefs"))
  }
}

kspAllConfigs(project(":aktual-codegen:ksp"))

buildConfig {
  packageName("aktual.test")

  sourceSets.named("test") {
    rootProject
      .isolated
      .projectDirectory
      .dir("api/actual")
      .asFile
      .listFiles()
      .orEmpty()
      .filter { it.isDirectory }
      .forEach { dir -> addResponsesClass(dir) }
  }
}

fun BuildConfigSourceSet.addResponsesClass(directory: File) {
  forClass(directory.name.capitalized() + "Responses") {
    directory
      .listFiles()
      .orEmpty()
      .filter { it.extension.lowercase() in setOf("json", "txt") }
      .forEach { file ->
        val name = file.nameWithoutExtension
          .replace("-", "_")
          .replace(".", "_")
          .uppercase()
        val regularFile = layout.projectDirectory.file(file.absolutePath)
        val value = providers.fileContents(regularFile).asText.map { content -> "\"\"\"\n$content\"\"\"" }
        buildConfigField("String", name, value)
      }
  }
}
