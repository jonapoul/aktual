package actual.diagrams.tasks

import actual.diagrams.ModuleType
import actual.diagrams.color
import actual.diagrams.label
import guru.nidi.graphviz.attribute.Attributes
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Rank.RankType
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Factory
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.parse.Parser
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
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register

@CacheableTask
abstract class GenerateDotFileTask : DefaultTask() {
  @get:[PathSensitive(RELATIVE) InputFile] abstract val linksFile: RegularFileProperty
  @get:[PathSensitive(RELATIVE) InputFile] abstract val moduleTypesFile: RegularFileProperty
  @get:Input abstract val thisPath: Property<String>
  @get:Input abstract val printOutput: Property<Boolean>
  @get:Input abstract val toRemove: Property<String>
  @get:Input abstract val replacement: Property<String>
  @get:OutputFile abstract val dotFile: RegularFileProperty

  @TaskAction
  fun execute() {
    val thisPath = thisPath.get()
    val linksFile = linksFile.get().asFile
    val moduleTypesFile = moduleTypesFile.get().asFile
    val links = ProjectLinks.read(linksFile)
    val typedModules = TypedModules.read(moduleTypesFile)

    val dotFileContents = generateGraph(links, typedModules, thisPath)
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

    fun register(
      target: Project,
      name: String,
      dotFile: Provider<RegularFile>,
      printOutput: Boolean,
    ) = with(target) {
      val toRemove = providers.gradleProperty("actual.diagram.removeModulePrefix")
      val replacement = providers.gradleProperty("actual.diagram.replacementModulePrefix")

      val task = tasks.register<GenerateDotFileTask>(name) {
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
          linksFile.set(calculateProjectTree.get().outputFile)
          moduleTypesFile.set(collateModuleTypes.get().outputFile)
        }
      }

      task
    }
  }
}

private fun generateGraph(links: Set<ProjectLink>, types: Set<TypedModule>, thisPath: String): MutableGraph {
  val projectPaths = links
    .map { it.fromPath }
    .plus(links.map { it.toPath })
    .toSet()

  val typeMap = types.associate { (path, type) -> path to type }

  val graph = Factory
    .mutGraph()
    .setDirected(true)
    .graphAttrs()
    .add(GraphAttr.dpi(DPI))

  graph.nodeAttrs().add(Style.FILLED)

  projectPaths.forEach { path ->
    val moduleType = requireNotNull(typeMap[path]) { "Null module type for $path in $typeMap" }
    val node = Factory.mutNode(path)
    node.add(moduleType.color)
    if (path == thisPath) {
      node.add(Color.BLACK, Attributes.attr("penwidth", "3"))
      node.add(Shape.BOX)
    } else {
      node.add(Shape.NONE)
    }
    graph.add(node)
  }

  graph.add(
    Factory
      .graph()
      .graphAttr()
      .with(Rank.inSubgraph(RankType.SAME))
      .with(Factory.mutNode(thisPath)),
  )

  val rootNodes = graph
    .rootNodes()
    .filterNotNull()
    .filter { it.links().isEmpty() }

  links
    .filter { it.fromPath != it.toPath }
    .distinctBy { it.fromPath to it.toPath }
    .forEach { (from, to, configuration) ->
      val fromNode = rootNodes.single { it.name().toString() == from }
      val toNode = rootNodes.singleOrNull { it.name().toString() == to } ?: return@forEach
      val link = Link.to(toNode)
      val styledLink = if (isImplementation(configuration)) link.with(Style.DOTTED) else link
      graph.add(fromNode.addLink(styledLink))
    }

  graph.addLegend(thisPath)

  graph.graphAttrs().add(
    Label.of(thisPath).locate(Label.Location.TOP),
    Font.size(LABEL_FONT_SIZE),
  )

  graph.graphAttrs().add(
    Rank.sep(RANK_SEPARATION),
    Font.size(NODE_FONT_SIZE),
    Rank.dir(Rank.RankDir.TOP_TO_BOTTOM),
  )

  return graph
}

private fun isImplementation(configuration: String) = configuration.contains("implementation", ignoreCase = true)

private fun MutableGraph.addLegend(thisPath: String) {
  val rootNode = rootNodes()
    .filterNotNull()
    .distinct()
    .firstOrNull { it.name().toString() == thisPath }
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
