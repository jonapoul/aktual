package aktual.gradle

import atlas.core.tasks.CollateProjectTypes
import atlas.core.tasks.WriteReadme
import atlas.d2.tasks.ExecD2
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute

class ConventionAtlas : ProjectPlugin {
  override fun Project.applyTo() {
    if (path == ":") registerIndex() else configureModule()
  }

  private fun Project.registerIndex() {
    // Atlas's own scope already depends on every charted module, so reuse it
    val charts =
      configurations.resolvable("atlasChartClasspath") { c ->
        c.extendsFrom(configurations.named(ATLAS_DEPENDENCY_SCOPE).get())
        c.attributes.attribute(ATLAS_ARTIFACT, CHART_ARTIFACT)
      }
    val artifacts = charts.map { c -> c.incoming.artifactView { it.lenient(true) }.artifacts }

    val writeIndex =
      tasks.register("atlasWriteIndex", WriteAtlasIndex::class.java) { t ->
        t.chartIds.set(artifacts.flatMap { a -> a.resolvedArtifacts.map { r -> r.map { it.id } } })
        t.chartFiles.from(artifacts.map { it.artifactFiles })
        t.projectTypes.set(
          tasks.named("collateProjectTypes", CollateProjectTypes::class.java).flatMap {
            it.outputFile
          },
        )
        t.outputFile.set(layout.buildDirectory.file("atlas/index.html"))
      }
    tasks.register("atlasGenerate") { t ->
      t.group = "atlas"
      t.dependsOn(writeIndex)
    }
  }

  private fun Project.configureModule() {
    val chartOutputDir = layout.buildDirectory.dir("atlas")

    // module-dir/build/atlas/chart.svg
    tasks.withType(ExecD2::class.java).configureEach { t ->
      t.outputFile.set(
        t.outputFormat.flatMap { format ->
          chartOutputDir.map { it.file("chart.$format") }
        },
      )
    }

    // Lets the root index every chart, see registerIndex
    configurations.consumable("atlasChartElements") { c ->
      c.attributes.attribute(ATLAS_ARTIFACT, CHART_ARTIFACT)
    }
    val chart = tasks.named("execD2Chart", ExecD2::class.java).flatMap { it.outputFile }
    artifacts.add("atlasChartElements", chart)

    // Charts aren't committed, the main workflow publishes them to GitHub Pages
    val localDir = chartOutputDir.map { it.asFile.relativeTo(projectDir).invariantSeparatorsPath }
    val remoteDir = "$PAGES_URL/${projectDir.relativeTo(rootDir).invariantSeparatorsPath}"

    tasks.withType(WriteReadme::class.java).configureEach { t ->
      t.inputs.property("remoteDir", remoteDir)
      t.doLast { task ->
        val readme = (task as WriteReadme).outputFile.get().asFile
        readme.writeText(readme.readText().replace("](${localDir.get()}/", "]($remoteDir/"))
      }
    }
  }

  private companion object {
    const val PAGES_URL = "https://jonapoul.github.io/aktual"
    const val ATLAS_DEPENDENCY_SCOPE = "atlasProjectTypeClasspathDependencies"
    const val CHART_ARTIFACT = "aktual-chart"
    val ATLAS_ARTIFACT: Attribute<String> =
      Attribute.of("dev.jonpoulton.atlas.artifact", String::class.java)
  }
}
