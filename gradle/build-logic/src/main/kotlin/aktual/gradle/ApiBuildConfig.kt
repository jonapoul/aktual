@file:Suppress("UnstableApiUsage")

package aktual.gradle

import com.github.gmazzo.buildconfig.BuildConfigExtension
import com.github.gmazzo.buildconfig.BuildConfigSourceSet
import java.io.File
import org.gradle.api.Project
import org.gradle.internal.extensions.stdlib.capitalized

fun BuildConfigExtension.generateApiResponses(project: Project, directory: String) {
  sourceSets.named("test") {
    project.rootProject.isolated.projectDirectory
      .dir(directory)
      .asFile
      .listFiles()
      .orEmpty()
      .filter { it.isDirectory }
      .forEach { dir -> addResponsesClass(project, dir) }
  }
}

fun BuildConfigSourceSet.addResponsesClass(project: Project, directory: File) {
  forClass(directory.name.capitalized() + "Responses") {
    directory
      .listFiles()
      .orEmpty()
      .filter { it.extension.lowercase() in ALLOWED_EXTENSIONS }
      .forEach { file ->
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

private val ALLOWED_EXTENSIONS = setOf("json", "txt", "css")
