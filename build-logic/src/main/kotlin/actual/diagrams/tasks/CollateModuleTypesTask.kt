package actual.diagrams.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CollateModuleTypesTask : DefaultTask() {
  @get:[PathSensitive(RELATIVE) InputFiles] abstract val projectTypeFiles: ConfigurableFileCollection
  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val outputFile = outputFile.get().asFile
    val modulesWithType = projectTypeFiles
      .filter { file -> file.exists() }
      .map { file -> TypedModule(file.readText()) }
      .toSortedSet()
    TypedModules.write(modulesWithType, outputFile)
  }

  companion object {
    private const val NAME = "collateModuleTypes"

    fun outputFile(target: Project) = target.fileInReportDirectory("project-types-all.txt")

    fun register(target: Project) = with(target) {
      val task = tasks.register<CollateModuleTypesTask>(NAME) {
        group = "reporting"
        outputFile.set(outputFile(target))
      }

      gradle.projectsEvaluated {
        val moduleTypeFiles = rootProject
          .subprojects
          .toList()
          .map(DumpModuleTypeTask::outputFile)

        task.configure {
          projectTypeFiles.from(moduleTypeFiles)
        }
      }

      task
    }
  }
}
