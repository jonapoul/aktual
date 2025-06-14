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
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CheckDotFileTask : DefaultTask() {
  @get:Input abstract val taskPath: Property<String>
  @get:[PathSensitive(RELATIVE) InputFile] abstract val expectedDotFile: RegularFileProperty
  @get:[PathSensitive(RELATIVE) InputFile] abstract val actualDotFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val expectedContents = expectedDotFile.get().asFile.readLines()
    val actualContents = actualDotFile.get().asFile.readLines()

    require(expectedContents == actualContents) {
      """
        Dotfile needs updating! Run `gradle ${taskPath.get()}` to regenerate ${expectedDotFile.get()}.

        Expected (len = ${expectedContents.size}):
        '${expectedContents.joinToString("\n")}'

        Actual (len = ${actualContents.size}):
        '${actualContents.joinToString("\n")}'
      """.trimIndent()
    }
  }

  companion object {
    fun register(target: Project, generateDotFile: TaskProvider<GenerateDotFileTask>, realDotFile: RegularFile) =
      with(target) {
        tasks.register<CheckDotFileTask>("checkDotFiles") {
          group = JavaBasePlugin.VERIFICATION_GROUP
          taskPath.set("$path:${GenerateDotFileTask.TASK_NAME}")
          expectedDotFile.set(generateDotFile.get().dotFile)
          actualDotFile.set(realDotFile)
        }
      }
  }
}
