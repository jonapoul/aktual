package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.ABSOLUTE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CheckReadmeTask : DefaultTask() {
  @get:Input abstract val taskPath: Property<String>
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val expectedReadme: RegularFileProperty
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val actualReadme: RegularFileProperty

  @TaskAction
  fun execute() {
    val expectedContents = expectedReadme.get().asFile.readLines()
    val actualContents = actualReadme.get().asFile.readLines()

    require(expectedContents == actualContents) {
      """
        Readme needs updating! Run `gradle ${taskPath.get()}` to regenerate ${expectedReadme.get()}.

        Expected (len = ${expectedContents.size}):
        '${expectedContents.joinToString("\n")}'

        Actual (len = ${actualContents.size}):
        '${actualContents.joinToString("\n")}'
      """.trimIndent()
    }
  }

  companion object {
    fun register(target: Project, generateMermaid: TaskProvider<GenerateReadmeTask>, realDotFile: RegularFile) =
      with(target) {
        tasks.register<CheckReadmeTask>("checkReadme") {
          group = JavaBasePlugin.VERIFICATION_GROUP
          taskPath.set("$path:${GenerateReadmeTask.TASK_NAME}")
          expectedReadme.set(generateMermaid.map { it.readme.get() })
          actualReadme.set(realDotFile)
        }
      }
  }
}
