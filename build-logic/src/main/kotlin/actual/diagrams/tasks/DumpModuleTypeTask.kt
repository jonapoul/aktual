package actual.diagrams.tasks

import actual.diagrams.ModuleType
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class DumpModuleTypeTask : DefaultTask() {
  @get:Input abstract val type: Property<ModuleType>
  @get:Input abstract val projectPath: Property<String>
  @get:OutputFile abstract val outputFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val type = type.get()
    val projectPath = projectPath.get()
    val outputFile = outputFile.get().asFile
    val typedModule = TypedModule(projectPath, type)
    outputFile.writeText(typedModule.toString())
  }

  companion object {
    private const val NAME = "dumpModuleType"

    fun get(target: Project) = try {
      target.tasks.named<DumpModuleTypeTask>(NAME)
    } catch (_: UnknownTaskException) {
      null
    }

    fun register(target: Project): TaskProvider<DumpModuleTypeTask> = with(target) {
      val task = tasks.register<DumpModuleTypeTask>(NAME) {
        group = "reporting"
        projectPath.set(target.path)
        outputFile.set(target.fileInReportDirectory("project-type.txt"))
      }

      target.afterEvaluate {
        task.configure {
          type.set(ModuleType.find(target))
        }
      }

      task
    }
  }
}
