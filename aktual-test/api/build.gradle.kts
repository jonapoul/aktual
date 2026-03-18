@file:Suppress("UnstableApiUsage")

import blueprint.core.commonMainDependencies
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import org.gradle.internal.extensions.stdlib.capitalized

plugins { id("aktual.module.kotlin") }

kotlin {
  commonMainDependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.ktor.core)
    api(libs.ktor.test)
    api(project(":aktual-test"))
    implementation(project(":aktual-core:api:impl"))
    implementation(project(":aktual-core:model"))
  }
}

val apiDir: Directory = rootProject.isolated.projectDirectory.dir("api")

buildConfig {
  packageName = "aktual.test"

  apiDir
    .dir("actual")
    .asFile
    .listFiles()
    .orEmpty()
    .filter { it.isDirectory }
    .forEach { addResponsesClass(it) }

  addResponsesClass(apiDir.dir("github").asFile)

  addResponsesClass(apiDir.dir("theme").asFile)
}

fun BuildConfigSourceSet.addResponsesClass(directory: File) {
  forClass(directory.name.capitalized() + "Responses") {
    useKotlinOutput { internalVisibility = false }

    directory.listFiles().orEmpty().filter(::shouldInclude).forEach { file ->
      val name = file.nameWithoutExtension.replace("-", "_").replace(".", "_").uppercase()
      val regularFile = project.layout.projectDirectory.file(file.absolutePath)
      val value =
        project.providers.fileContents(regularFile).asText.map { content ->
          "\"\"\"\n$content\"\"\""
        }
      buildConfigField("String", name, value)
    }
  }
}

// Avoids "error: string too large to encode using UTF-8 written instead as 'STRING_TOO_LARGE'."
fun shouldInclude(f: File): Boolean = f.extension.lowercase() != "rest" && f.length() < 100_000L
