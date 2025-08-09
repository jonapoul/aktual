package actual.diagrams.tasks

import actual.diagrams.ModuleType
import actual.diagrams.color
import actual.diagrams.label
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
abstract class GenerateReadmeTask : DefaultTask() {
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val linksFile: RegularFileProperty
  @get:[PathSensitive(ABSOLUTE) InputFile] abstract val moduleTypesFile: RegularFileProperty
  @get:Input abstract val thisPath: Property<String>
  @get:Input abstract val printOutput: Property<Boolean>
  @get:Input abstract val toRemove: Property<String>
  @get:Input abstract val replacement: Property<String>
  @get:OutputFile abstract val readme: RegularFileProperty

  @TaskAction
  fun execute() {
    val linksFile = linksFile.get().asFile
    val moduleTypesFile = moduleTypesFile.get().asFile
    val toRemove = toRemove.get()
    val replacement = replacement.get()
    fun String.cleaned() = replace(toRemove, replacement).removePrefix(":")
    val thisPath = thisPath.get().cleaned()

    val links = ProjectLinks
      .read(linksFile)
      .map { l -> l.copy(fromPath = l.fromPath.cleaned(), toPath = l.toPath.cleaned()) }
      .toSet()

    val typeMap = TypedModules
      .read(moduleTypesFile)
      .associate { (path, type) -> path.cleaned() to type }

    val nodes = links
      .map { it.fromPath }
      .plus(links.map { it.toPath })
      .plus(thisPath)
      .map { path -> path.cleaned().let { Node(path = it, type = typeMap.getValue(it)) } }
      .sortedBy { it.path }
      .toSet()

    val usedTypes = nodes
      .map { it.type }
      .sorted()
      .toSet()

    val legendMarkdown = generateLegendContents(usedTypes)
    val mermaid = generateMermaidContents(links, nodes)

    val readmeContents = buildString {
      appendLine("# $thisPath")
      appendLine()
      append(legendMarkdown)
      appendLine()
      appendLine("```mermaid")
      append(mermaid)
      appendLine("```")
    }

    val readmeFile = readme.get().asFile
    readmeFile.sink().buffer().use { sink -> sink.writeUtf8(readmeContents) }

    if (printOutput.get()) {
      logger.lifecycle(readmeFile.absolutePath)
    }
  }

  companion object {
    const val TASK_NAME = "generateReadme"

    fun register(
      target: Project,
      name: String,
      readme: Provider<RegularFile>,
      printOutput: Boolean,
    ) = with(target) {
      val toRemove = providers.gradleProperty("actual.diagram.removeModulePrefix")
      val replacement = providers.gradleProperty("actual.diagram.replacementModulePrefix")

      val task = tasks.register<GenerateReadmeTask>(name) {
        group = "reporting"
        description = "Generates readme, containing a project dependency graph for $path"
        this.readme.set(readme)
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

// :modules:core:test-whatever -> modulescoretestwhatever
private fun generateName(label: String) = label
  .replace(":", "")
  .replace("-", "")

private data class Node(
  val path: String,
  val type: ModuleType,
)

private fun generateLegendContents(usedTypes: Set<ModuleType>): String = buildString {
  appendLine("<table>")
  appendLine("<tr><th colspan='2'>Legend</th></tr>")

  for (type in usedTypes) {
    val label = "<td style='text-align:center;'>${type.label}</td>"
    val value = "<td style='text-align:center; background-color:${type.color}; color:black'>module-name</td>"
    appendLine("<tr>$label$value</tr>")
  }

  appendLine("</table>")
}

private fun generateMermaidContents(
  links: Set<ProjectLink>,
  nodes: Set<Node>,
): String = buildString {
  appendLine("graph TD")
  appendLine("classDef titleStyle fill:none,stroke:none,font-size:24px,font-weight:bold")

  val declarations = mutableListOf<String>()
  val styles = mutableListOf<String>()
  for ((path, type) in nodes) {
    val name = generateName(path)
    declarations += "$name[\"$path\"]"
    styles += "style $name fill:${type.color},stroke:#333,stroke-width:2px,color:black,font-weight:bold"
  }

  declarations.forEach(::appendLine)
  styles.forEach(::appendLine)

  for ((from, to, config) in links) {
    val fromName = generateName(from)
    val toName = generateName(to)
    val styledLink = if (isImplementation(config)) "-.->" else "-->"
    appendLine("$fromName $styledLink $toName")
  }
}

private fun isImplementation(configuration: String) = configuration.contains("implementation", ignoreCase = true)
