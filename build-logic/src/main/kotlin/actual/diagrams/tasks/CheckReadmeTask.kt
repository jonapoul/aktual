package actual.diagrams.tasks

import actual.diagrams.FILENAME_ROOT
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CheckReadmeTask : DefaultTask() {
  @get:[PathSensitive(RELATIVE) InputFile] abstract val readmeFile: RegularFileProperty
  @get:Input abstract val projectPath: Property<String>

  @TaskAction
  fun execute() {
    val readmeFile = readmeFile.get().asFile
    val actualContents = readmeFile.readText()
    val expectedTitle = projectPath.get().removePrefix(prefix = ":")
    val expectedContents = """
      # $expectedTitle

      ![$FILENAME_ROOT.png]($FILENAME_ROOT.png)

    """.trimIndent()

    if (expectedContents != actualContents) {
      val msg = """
        Expected contents in $readmeFile like:

        '$expectedContents'

        but got contents like:

        '$actualContents'
      """.trimIndent()
      throw GradleException(msg)
    }
  }

  companion object {
    fun register(target: Project) = with(target) {
      val removeModulePrefix = providers.gradleProperty("actual.diagram.removeModulePrefix")
      tasks.register<CheckReadmeTask>("checkModulesReadme") {
        group = JavaBasePlugin.VERIFICATION_GROUP
        readmeFile.set(file("README.md"))
        projectPath.set(target.path.removePrefix(removeModulePrefix.get()))
      }
    }
  }
}
