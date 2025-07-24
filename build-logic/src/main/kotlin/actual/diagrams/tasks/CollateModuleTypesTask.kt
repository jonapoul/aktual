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
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class CollateModuleTypesTask : DefaultTask() {
  @get:[PathSensitive(RELATIVE) InputFiles] abstract val projectTypeFiles: ConfigurableFileCollection
  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val outputFile = outputFile.get().asFile
    val modulesWithType = projectTypeFiles
      .map { file -> TypedModule(file.readText()) }
      .toSortedSet()
    TypedModules.write(modulesWithType, outputFile)
  }

  companion object {
    private const val NAME = "collateModuleTypes"

    fun get(target: Project) = target.tasks.named<CollateModuleTypesTask>(NAME)

    fun register(target: Project) = with(target) {
      val task = tasks.register<CollateModuleTypesTask>(NAME) {
        group = "reporting"
        outputFile.set(fileInReportDirectory("project-types-all.txt"))
      }

      gradle.projectsEvaluated {
        task.configure {
          val dumpTasks = rootProject
            .subprojects
            .toList()
            .mapNotNull(DumpModuleTypeTask::get)
          dependsOn(dumpTasks)
          projectTypeFiles.from(
            dumpTasks.map { taskProvider ->
              taskProvider.map { outputFile.get() }
            }
          )
        }
      }

      task
    }
  }
}
