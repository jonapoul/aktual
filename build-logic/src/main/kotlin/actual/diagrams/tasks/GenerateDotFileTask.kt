@file:Suppress("SpreadOperator")

package actual.diagrams.tasks

import actual.diagrams.ModuleType
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Rank.RankType
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.mutNode
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.parse.Parser
import okio.buffer
import okio.sink
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault
open class GenerateDotFileTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {
  @get:Input val printOutput: Property<Boolean> = objects.property()
  @get:Input val toRemove: Property<String> = objects.property(String::class.java)
  @get:Input val replacement: Property<String> = objects.property(String::class.java)
  @get:OutputFile val dotFile: RegularFileProperty = objects.fileProperty()

  init {
    group = "reporting"
    outputs.cacheIf { false }
  }

  @TaskAction
  fun execute() {
    val dotFileContents = generateGraph(project)
      .toString()
      .replace(toRemove.get(), replacement.get())
      .withoutUnusedModuleTypes()
      .withSortedModuleDeclarations()
      .withSortedModuleLinks()
      .withoutEmptyLines()

    val dotFile = dotFile.get().asFile
    dotFile.sink().buffer().use { sink ->
      sink.writeUtf8(dotFileContents)
    }

    if (printOutput.get()) {
      logger.lifecycle(dotFile.absolutePath)
    }
  }

  companion object {
    const val TASK_NAME = "generateModulesDotFile"
  }
}

private fun generateGraph(project: Project): MutableGraph {
  val projects = mutableSetOf<Project>()
  val dependencies = mutableListOf<ProjectDependencyContainer>()

  project.allprojects
    .filter { it.isDependingOnOtherProject() }
    .forEach { addProject(it, projects, dependencies) }

  val graph = mutGraph()
    .setDirected(true)
    .graphAttrs()
    .add(GraphAttr.dpi(DPI))

  graph.nodeAttrs().add(Style.FILLED)

  projects.forEach { proj ->
    val moduleType = ModuleType.find(proj)
    val node = mutNode(proj.path)
    node.add(moduleType.color)
    node.add(Shape.NONE)
    graph.add(node)
  }

  val nodes = projects
    .filter { proj -> dependencies.none { it.to == proj } }.map { mutNode(it.path) }
    .toTypedArray()

  graph.add(
    Factory.graph()
      .graphAttr()
      .with(Rank.inSubgraph(RankType.SAME))
      .with(*nodes),
  )

  val rootNodes = graph
    .rootNodes()
    .filterNotNull()
    .filter { it.links().isEmpty() }

  dependencies
    .filterNot { (from, to, _) -> from == to }
    .distinctBy { it.from.path to it.to.path }
    .forEach { (from, to, configuration) ->
      val fromNode = rootNodes.single { it.name().toString() == from.path }
      val toNode = rootNodes.singleOrNull { it.name().toString() == to.path } ?: return@forEach
      val link = Link.to(toNode)
      val styledLink = if (configuration.isImplementation()) {
        link.with(Style.DOTTED)
      } else {
        link
      }
      graph.add(fromNode.addLink(styledLink))
    }

  graph.addLegend(project)

  graph.graphAttrs().add(
    Label.of(project.path).locate(Label.Location.TOP),
    Font.size(LABEL_FONT_SIZE),
  )

  graph.graphAttrs().add(
    Rank.sep(RANK_SEPARATION),
    Font.size(NODE_FONT_SIZE),
    Rank.dir(Rank.RankDir.TOP_TO_BOTTOM),
  )

  return graph
}

private fun addProject(
  project: Project,
  projects: MutableSet<Project>,
  dependencies: MutableList<ProjectDependencyContainer>,
) {
  if (project.shouldInclude() && projects.add(project)) {
    project.configurations
      .filter { conf -> conf.shouldInclude() }
      .flatMap { conf ->
        conf.dependencies
          .withType(ProjectDependency::class.java)
          .map { ProjectDependencyContainer(project, project.project(it.path), conf) }
      }.forEach {
        dependencies.add(it)
        addProject(it.to, projects, dependencies)
      }
  }
}

private fun Project.shouldInclude(): Boolean {
  val isRoot = this == rootProject
  val isTest = path.contains("test", ignoreCase = true)
  val isKsp = path.contains("ksp", ignoreCase = true)
  return !isRoot && !isTest && !isKsp
}

private fun Configuration.shouldInclude(): Boolean =
  !name.contains("test", ignoreCase = true) && !name.startsWith("ios")

private fun Project.isDependingOnOtherProject(): Boolean =
  configurations.any { c -> c.dependencies.any { dep -> dep is ProjectDependency } }

private fun Configuration.isImplementation() = name.lowercase().endsWith("implementation")

private fun MutableGraph.addLegend(project: Project) {
  val rootName = project.path
  val rootNode = rootNodes()
    .filterNotNull()
    .distinct()
    .firstOrNull { it.name().toString() == rootName }
    ?: return

  // Add the actual legend
  val legend = buildLegend()
  add(legend)

  // Add a link from the legend to the root module
  val link = Link.to(rootNode).with(Style.INVIS)
  add(legend.addLink(link))
}

private fun buildLegend(): MutableGraph {
  val rows = ModuleType.values().map { type ->
    "<TR><TD>${type.label}</TD><TD BGCOLOR=\"${type.color.value}\">module-name</TD></TR>"
  }
  return Parser().read(
    """
      graph cluster_legend {
        label="$LEGEND_LABEL"
        graph [fontsize=$LEGEND_TITLE_FONT_SIZE]
        node [style=filled, fillcolor="$LEGEND_BACKGROUND"];
        Legend [shape=none, margin=0, fontsize=$LEGEND_FONT_SIZE, label=<
          <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="4">
            ${rows.joinToString(separator = "\n")}
          </TABLE>
        >];
      }
    """.trimIndent(),
  )
}

private data class ProjectDependencyContainer(
  val from: Project,
  val to: Project,
  val configuration: Configuration,
)

private fun String.withoutUnusedModuleTypes(): String = buildString {
  val usedColours = mutableSetOf<String>()
  val moduleTypeColorsAppended = mutableSetOf<String>()

  val lines = this@withoutUnusedModuleTypes.lineSequence()
  for (line in lines) {
    val fillColorMatch = FILL_COLOR_REGEX.matchEntire(line)
    if (fillColorMatch != null) {
      val color = fillColorMatch.groupValues[1]
      usedColours.add(color)
    }

    val tableLineMatch = TABLE_LINE_REGEX.matchEntire(line)
    if (tableLineMatch != null) {
      val color = tableLineMatch.groupValues[1]
      if (color in usedColours && color !in moduleTypeColorsAppended) {
        appendLine(line)
        moduleTypeColorsAppended.add(color)
      }
      continue
    }

    appendLine(line)
  }
}

private fun String.withSortedModuleDeclarations(): String = buildString {
  val lines = this@withSortedModuleDeclarations.lineSequence()
  val moduleDeclarations = mutableSetOf<String>()
  var isInModuleDeclarations = false

  for (line in lines) {
    if (line.startsWith("\":") && line.contains("fillcolor")) {
      isInModuleDeclarations = true
      moduleDeclarations.add(line)
    } else if (isInModuleDeclarations) {
      // we were in declarations before but not now, so print them all sorted
      isInModuleDeclarations = false
      for (module in moduleDeclarations.sorted()) {
        appendLine(module)
      }
    }

    if (!isInModuleDeclarations) {
      appendLine(line)
    }
  }

  appendLine()
}

private fun String.withSortedModuleLinks(): String = buildString {
  val lines = this@withSortedModuleLinks.lineSequence()
  val linkDeclarations = mutableSetOf<String>()
  var isInLinkDeclarations = false

  for (line in lines) {
    if (line.startsWith("\":") && line.contains("->")) {
      isInLinkDeclarations = true
      linkDeclarations.add(line)
    } else if (isInLinkDeclarations) {
      // we were in declarations before but not now, so print them all sorted
      isInLinkDeclarations = false
      for (module in linkDeclarations.sorted()) {
        appendLine(module)
      }
    }

    if (!isInLinkDeclarations) {
      appendLine(line)
    }
  }

  appendLine()
}

private fun String.withoutEmptyLines(): String = buildString {
  for (line in this@withoutEmptyLines.lineSequence()) {
    if (line.isNotEmpty()) appendLine(line)
  }
}

private const val DPI = 100
private const val LABEL_FONT_SIZE = 35
private const val LEGEND_BACKGROUND = "#FFFFFF"
private const val LEGEND_FONT_SIZE = 15
private const val LEGEND_LABEL = "Legend"
private const val LEGEND_TITLE_FONT_SIZE = 20
private const val NODE_FONT_SIZE = 30
private const val RANK_SEPARATION = 1.5

private val FILL_COLOR_REGEX = """
      ^.*?"fillcolor"="#(\w+)".*?$
    """.trimIndent().toRegex()

private val TABLE_LINE_REGEX = """
      ^<TR><TD>.*?</TD><TD BGCOLOR="#(.*?)">module-name</TD></TR>$
    """.trimIndent().toRegex()
