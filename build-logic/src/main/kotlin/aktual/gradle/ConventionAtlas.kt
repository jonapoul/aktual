package aktual.gradle

import atlas.core.tasks.WriteReadme
import atlas.d2.tasks.ExecD2
import org.gradle.api.Project

class ConventionAtlas : ProjectPlugin {
  override fun Project.applyTo() {
    val chartOutputDir = layout.buildDirectory.dir("atlas")

    // module-dir/build/atlas/chart.svg
    tasks.withType(ExecD2::class.java).configureEach { t ->
      t.outputFile.set(
        t.outputFormat.flatMap { format ->
          chartOutputDir.map { it.file("chart.$format") }
        },
      )
    }

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
  }
}
