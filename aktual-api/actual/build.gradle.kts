@file:Suppress("UnstableApiUsage")

import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.internal.extensions.stdlib.capitalized

plugins {
  alias(libs.plugins.module.multiplatform)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.redacted)
  alias(libs.plugins.convention.buildconfig)
}

kotlin {
  commonMainDependencies {
    api(libs.alakazam.kotlin.core)
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(libs.okio)
    api(project(":aktual-budget:model"))
    api(project(":aktual-core:model"))
    implementation(libs.ktor.serialization.core)
    implementation(libs.ktor.serialization.json)
    implementation(project(":aktual-budget:data"))
    implementation(project(":aktual-codegen:annotation"))
    compileOnly(libs.redacted.annotations)
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
