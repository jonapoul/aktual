package actual.gradle

import actual.diagrams.CheckDotFileTask
import actual.diagrams.CheckReadmeTask
import actual.diagrams.DiagramsBlueprintExtension
import actual.diagrams.FILENAME_ROOT
import actual.diagrams.GenerateGraphVizPngTask
import actual.diagrams.TidyDotFileTask
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorPlugin
import com.vanniktech.dependency.graph.generator.ProjectDependencyGraphGeneratorTask
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.parse.Parser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import java.io.File

class ConventionDiagrams : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    pluginManager.apply(DependencyGraphGeneratorPlugin::class)

    val extension = extensions.create("diagrams", DiagramsBlueprintExtension::class.java)
    registerModuleTasks(extension)

    val checkReadmeTask = tasks.register<CheckReadmeTask>(name = "checkDiagramReadme") {
      enabled = extension.checkReadmeContents.get()
      readmeFile.set(file("README.md"))
      projectPath.set(project.path)
    }

    tasks.named("check").configure {
      dependsOn(checkReadmeTask)
    }
  }

  private fun Project.registerModuleTasks(extension: DiagramsBlueprintExtension) {
    val projectGenerator = ProjectGenerator(
      outputFormats = listOf(),
      projectNode = { node, proj ->
        val moduleTypeFinder = extension.moduleTypeFinder.get()
        node.add(Shape.NONE).add(moduleTypeFinder.color(proj))
      },
      includeProject = { proj -> proj.shouldInclude() },
      graph = { graph ->
        if (extension.showLegend.get()) graph.addLegend(extension)
        graph.graphAttrs().add(
          Label.of(project.path).locate(Label.Location.TOP),
          Font.size(LABEL_FONT_SIZE),
        )
        graph.graphAttrs().add(
          Rank.sep(extension.rankSeparation.get()),
          Font.size(extension.nodeFontSize.get()),
          Rank.dir(extension.rankDir.get()),
        )
      },
    )
    val dotTask = tasks.register<ProjectDependencyGraphGeneratorTask>("generateModulesDotfile") {
      group = "reporting"
      description = "Generates a project dependency graph for $path"
      this.projectGenerator = projectGenerator
      outputDirectory = projectDir
    }

    val tidyDotFileTask = tasks.register<TidyDotFileTask>("tidyDotFile") {
      dotFile.set(dotTask.get().getOutputFile())
      toRemove.set(extension.removeModulePrefix)
      replacement.set(extension.replacementModulePrefix)
      dependsOn(dotTask)
    }

    val tempDotTask = tasks.register<ProjectDependencyGraphGeneratorTask>("tempModulesDotfile") {
      group = JavaBasePlugin.VERIFICATION_GROUP
      this.projectGenerator = projectGenerator
      outputDirectory = project.layout.buildDirectory.dir("diagrams-modules-temp").get().asFile
    }

    val tempTidyDotFileTask = tasks.register<TidyDotFileTask>("tempTidyDotFile") {
      dotFile.set(tempDotTask.get().getOutputFile())
      toRemove.set(extension.removeModulePrefix)
      replacement.set(extension.replacementModulePrefix)
      dependsOn(tempDotTask)
    }

    // Make sure configuration cache is disabled when running this!
    tasks.register<CheckDotFileTask>("checkModulesDotfile") {
      group = JavaBasePlugin.VERIFICATION_GROUP
      taskPath.set(dotTask.get().path)
      expectedDotFile.set(dotTask.get().getOutputFile())
      actualDotFile.set(tempDotTask.get().getOutputFile())
      dependsOn(tempTidyDotFileTask)
    }

    tasks.register<GenerateGraphVizPngTask>("generateModulesPng") {
      reportDir.convention(layout.projectDirectory)
      dotFile.convention(reportDir.file("$FILENAME_ROOT.dot"))
      pngFile.convention(reportDir.file("$FILENAME_ROOT.png"))
      errorFile.convention(reportDir.file("$FILENAME_ROOT-error.log"))
      dependsOn(tidyDotFileTask)
    }
  }

  private fun MutableGraph.addLegend(extension: DiagramsBlueprintExtension) {
    // Find the root module, probably ":app"
    val rootName = extension.topLevelProject.get()
    val rootNode = rootNodes()
      .filterNotNull()
      .distinct()
      .firstOrNull { it.name().toString() == rootName }
      ?: return

    // Add the actual legend
    val legend = buildLegend(extension)
    add(legend)

    // Add a link from the legend to the root module
    val link = Link.to(rootNode).with(Style.INVIS)
    add(legend.addLink(link))
  }

  private fun buildLegend(extension: DiagramsBlueprintExtension): MutableGraph {
    val rows = extension.moduleTypes.get().map { type ->
      "<TR><TD>${type.string}</TD><TD BGCOLOR=\"${type.color.value}\">module-name</TD></TR>"
    }
    return Parser().read(
      """
        graph cluster_legend {
          label="$LEGEND_LABEL"
          graph [fontsize=${extension.legendTitleFontSize.get()}]
          node [style=filled, fillcolor="${extension.legendBackground.get()}"];
          Legend [shape=none, margin=0, fontsize=${extension.legendFontSize.get()}, label=<
            <TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="4">
              ${rows.joinToString(separator = "\n")}
            </TABLE>
          >];
        }
      """.trimIndent(),
    )
  }

  private fun Project.shouldInclude(): Boolean {
    val isRoot = this == rootProject
    val isTest = path.contains("test", ignoreCase = true)
    val isKsp = path.contains("ksp", ignoreCase = true)
    return !isRoot && !isTest && !isKsp
  }

  private fun ProjectDependencyGraphGeneratorTask.getOutputFile(): File = File(outputDirectory, "$FILENAME_ROOT.dot")

  private companion object {
    const val LEGEND_LABEL = "Legend"
    const val LABEL_FONT_SIZE = 35
  }
}
