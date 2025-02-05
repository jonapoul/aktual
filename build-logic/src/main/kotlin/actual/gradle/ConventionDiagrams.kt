package actual.gradle

import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.Generator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorExtension.ProjectGenerator
import com.vanniktech.dependency.graph.generator.DependencyGraphGeneratorTask
import com.vanniktech.dependency.graph.generator.ProjectDependencyGraphGeneratorTask
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.parse.Parser
import actual.diagrams.CheckDotFileTask
import actual.diagrams.CheckReadmeTask
import actual.diagrams.DiagramsBlueprintExtension
import actual.diagrams.GenerateGraphVizPngTask
import actual.diagrams.TidyDotFileTask
import actual.diagrams.color
import actual.diagrams.internal.depColour
import actual.diagrams.internal.getOutputFile
import actual.diagrams.internal.toNiceString
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

class ConventionDiagrams : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    pluginManager.apply("com.vanniktech.dependency.graph.generator")
    val extension = extensions.create("diagrams", DiagramsBlueprintExtension::class.java)

    val modulePngTask = registerModuleTasks(extension)
    val dependenciesPngTask = registerDependencyTasks(extension)

    // Aggregate task
    tasks.register("generatePngs", Task::class.java) {
      dependsOn(modulePngTask)
      dependsOn(dependenciesPngTask)
    }

    // Also check readme contents
    tasks.register<CheckReadmeTask>(name = "checkDiagramReadme") {
      enabled = extension.checkReadmeContents.get()
      readmeFile.set(file("README.md"))
      tasks.findByName("check")?.dependsOn(this)
    }
  }

  private fun Project.registerModuleTasks(
    extension: DiagramsBlueprintExtension,
  ): TaskProvider<GenerateGraphVizPngTask> {
    val projectGenerator = ProjectGenerator(
      outputFormats = listOf(),
      projectNode = { node, proj ->
        val moduleTypeFinder = extension.moduleTypeFinder.get()
        node.add(Shape.BOX).add(moduleTypeFinder.color(proj))
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
      outputDirectory = project.layout.buildDirectory.file("diagrams-modules-temp").get().asFile
    }

    val tempTidyDotFileTask = tasks.register<TidyDotFileTask>("tempTidyDotFile") {
      dotFile.set(tempDotTask.get().getOutputFile())
      toRemove.set(extension.removeModulePrefix)
      replacement.set(extension.replacementModulePrefix)
      dependsOn(tempDotTask)
    }

    tasks.register<CheckDotFileTask>("checkModulesDotfile") {
      group = JavaBasePlugin.VERIFICATION_GROUP
      taskPath.set(dotTask.get().path)
      expectedDotFile.set(dotTask.get().getOutputFile())
      actualDotFile.set(tempDotTask.get().getOutputFile())
      dependsOn(tempTidyDotFileTask)
      if (extension.checkModulesDotfile.get()) {
        tasks.findByName("check")?.dependsOn(this)
      }
    }

    return tasks.register<GenerateGraphVizPngTask>("generateModulesPng") {
      reportDir.convention(layout.projectDirectory)
      dotFile.convention(reportDir.file("project-dependency-graph.dot"))
      pngFile.convention(reportDir.file("project-dependency-graph.png"))
      errorFile.convention(reportDir.file("project-dependency-error.log"))
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
      "<TR><TD>${type.string}</TD><TD BGCOLOR=\"${type.color}\">module-name</TD></TR>"
    }
    return Parser().read(
      """
        graph cluster_legend {
          label="$LEGEND_LABEL"
          graph [fontsize=${extension.legendTitleFontSize.get()}]
          node [style=filled, fillcolor="${extension.legendBackground.get()}"];
          Legend [shape=none, margin=0, fontsize=${extension.legendFontSize.get()}, label=<
            <TABLE BORDER="0" CELLBORDER="1" CELLSPACING="0" CELLPADDING="4">
              ${rows.joinToString(separator = "\n")}
            </TABLE>
          >];
        }
      """.trimIndent(),
    )
  }

  private fun Project.registerDependencyTasks(
    extension: DiagramsBlueprintExtension,
  ): TaskProvider<GenerateGraphVizPngTask> {
    val dependencyGenerator = Generator(
      outputFormats = listOf(),
      dependencyNode = { node, dep ->
        node
          .add(Shape.RECTANGLE)
          .add(dep.depColour())
          .add(Label.of(dep.toNiceString(target = this)))
      },
      graph = { graph ->
        graph
          .graphAttrs().add(Rank.sep(extension.rankSeparation.get()), Font.size(extension.nodeFontSize.get()))
          .nodeAttrs().add(Style.FILLED)
      },
    )

    val dotTask = tasks.register("generateDependenciesDotfile", DependencyGraphGeneratorTask::class.java) {
      group = "reporting"
      description = "Generates a dependency graph for $path"
      generator = dependencyGenerator
      outputDirectory = projectDir
    }

    val tempDotTask = tasks.register("tempDependenciesDotfile", DependencyGraphGeneratorTask::class.java) {
      group = JavaBasePlugin.VERIFICATION_GROUP
      generator = dependencyGenerator
      outputDirectory = project.layout.buildDirectory.file("diagrams-dependencies-temp").get().asFile
    }

    tasks.register("checkDependenciesDotfile", CheckDotFileTask::class.java) {
      group = JavaBasePlugin.VERIFICATION_GROUP
      taskPath.set(dotTask.get().path)
      expectedDotFile.set(dotTask.get().getOutputFile())
      actualDotFile.set(tempDotTask.get().getOutputFile())
      dependsOn(tempDotTask)
      if (extension.checkDependenciesDotfile.get()) {
        tasks.findByName("check")?.dependsOn(this)
      }
    }

    return tasks.register("generateDependenciesPng", GenerateGraphVizPngTask::class.java) {
      reportDir.convention(layout.projectDirectory)
      dotFile.convention(reportDir.file("dependency-graph.dot"))
      pngFile.convention(reportDir.file("dependency-graph.png"))
      errorFile.convention(reportDir.file("dependency-error.log"))
      dependsOn(dotTask)
    }
  }

  private fun Project.shouldInclude(): Boolean {
    val isRoot = this == rootProject
    val isTest = path.contains("test", ignoreCase = true)
    return !isRoot && !isTest
  }

  private companion object {
    const val LEGEND_LABEL = "Legend"
    const val LABEL_FONT_SIZE = 35
  }
}
