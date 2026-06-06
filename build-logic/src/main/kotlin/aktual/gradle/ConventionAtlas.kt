package aktual.gradle

import atlas.graphviz.tasks.ExecGraphviz
import atlas.graphviz.tasks.WriteGraphvizChart
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Put atlas PNGs in the root of the module, not in `module/atlas/chart.png`. */
class ConventionAtlas : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.withPlugin("dev.jonpoulton.atlas.graphviz") {
        val atlasBuildDir = layout.buildDirectory.dir("atlas")

        tasks.withType(WriteGraphvizChart::class.java) { t ->
          t.outputFile.set(atlasBuildDir.map { d -> d.file("atlas.dot") })
        }

        tasks.withType(ExecGraphviz::class.java) { t ->
          t.outputFile.set(layout.projectDirectory.file("atlas.png"))
        }
      }
    }
}
