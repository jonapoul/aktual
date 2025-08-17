package actual.diagrams.tasks

import actual.diagrams.color
import okio.buffer
import okio.sink
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity.ABSOLUTE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class GenerateModulesDotFileTask : DefaultTask() {
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val linksFile: RegularFileProperty
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val moduleTypesFile: RegularFileProperty
  @get:Input abstract val thisPath: Property<String>
  @get:Input abstract val printOutput: Property<Boolean>
  @get:Input abstract val toRemove: Property<String>
  @get:Input abstract val replacement: Property<String>
  @get:OutputFile abstract val dotFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val linksFile = linksFile.get().asFile
    val moduleTypesFile = moduleTypesFile.get().asFile
    val thisPath = thisPath.get().cleaned()
    val links = ProjectLinks.read(linksFile)
    val typedModules = TypedModules.read(moduleTypesFile)

    val dotFileContents = buildString {
      appendLine("digraph {")
      appendHeader()
      appendNodes(typedModules, links, thisPath)
      appendLinks(links)
      appendLine("}")
    }

    val dotFile = dotFile.get().asFile
    dotFile.sink().buffer().use { sink ->
      sink.writeUtf8(dotFileContents)
    }

    if (printOutput.get()) {
      logger.lifecycle(dotFile.absolutePath)
    }
  }

  private fun StringBuilder.appendHeader() {
    appendLine("edge [\"dir\"=\"forward\"]")
    appendLine("graph [\"dpi\"=\"100\",\"fontsize\"=\"30\",\"ranksep\"=\"1.5\",\"rankdir\"=\"TB\"]")
    appendLine("node [\"style\"=\"filled\"]")
  }

  private fun StringBuilder.appendNodes(
    typedModules: Set<TypedModule>,
    links: Set<ProjectLink>,
    thisPath: String,
  ) {
    typedModules
      .filter { module -> module in links }
      .map { it.copy(projectPath = it.projectPath.cleaned()) }
      .sortedBy { module -> module.projectPath }
      .forEach { module ->
        val attrs = if (module.projectPath == thisPath) {
          "\"color\"=\"black\",\"penwidth\"=\"3\",\"shape\"=\"box\""
        } else {
          "\"shape\"=\"none\""
        }

        appendLine("\"${module.projectPath}\" [\"fillcolor\"=\"${module.type.color}\",$attrs]")
      }
  }

  private fun StringBuilder.appendLinks(links: Set<ProjectLink>) {
    links
      .map { link -> link.copy(fromPath = link.fromPath.cleaned(), toPath = link.toPath.cleaned()) }
      .sortedWith(compareBy({ it.fromPath }, { it.toPath }))
      .forEach { (fromPath, toPath, configuration) ->
        val attrs = if (configuration.contains("implementation", ignoreCase = true)) {
          " [\"style\"=\"dotted\"]"
        } else {
          ""
        }
        appendLine("\"$fromPath\" -> \"$toPath\"$attrs")
      }
  }

  private operator fun Set<ProjectLink>.contains(module: TypedModule): Boolean =
    any { (from, to, _) -> from == module.projectPath || to == module.projectPath }

  private fun String.cleaned() = replace(toRemove.get(), replacement.get())

  companion object {
    const val TASK_NAME = "generateModulesDotFile"

    fun register(
      target: Project,
      name: String,
      dotFile: Provider<RegularFile>,
      printOutput: Boolean,
    ) = with(target) {
      val toRemove = providers.gradleProperty("actual.diagram.removeModulePrefix")
      val replacement = providers.gradleProperty("actual.diagram.replacementModulePrefix")

      val task = tasks.register<GenerateModulesDotFileTask>(name) {
        group = "reporting"
        description = "Generates a project dependency graph for $path"
        this.dotFile.set(dotFile)
        this.toRemove.set(toRemove)
        this.replacement.set(replacement)
        this.printOutput.set(printOutput)
        this.thisPath.set(target.path)
      }

      gradle.projectsEvaluated {
        val collateModuleTypes = CollateModuleTypesTask.get(rootProject)
        val calculateProjectTree = CalculateProjectTreeTask.get(target)
        task.configure {
          linksFile.set(calculateProjectTree.map { it.outputFile.get() })
          moduleTypesFile.set(collateModuleTypes.map { it.outputFile.get() })
        }
      }

      task
    }
  }
}
