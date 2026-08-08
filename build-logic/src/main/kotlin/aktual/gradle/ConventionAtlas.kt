package aktual.gradle

import atlas.d2.tasks.ExecD2
import atlas.d2.tasks.SvgToPng
import atlas.d2.tasks.WriteD2Chart
import atlas.d2.tasks.WriteD2Classes
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Put atlas PNGs in the root of the module, not in `module/atlas/chart.png`. */
class ConventionAtlas : Plugin<Project> {
  override fun apply(target: Project): Unit =
    with(target) {
      val atlasBuildDir = layout.buildDirectory.dir("atlas")

      tasks.withType(WriteD2Chart::class.java) { t ->
        t.outputFile.set(atlasBuildDir.map { d -> d.file("atlas.d2") })
      }

      tasks.withType(ExecD2::class.java) { t ->
        t.outputFile.set(
          t.outputFormat.flatMap { format ->
            atlasBuildDir.map { dir ->
              dir.file("chart.${format.string}")
            }
          }
        )
      }

      tasks.withType(WriteD2Classes::class.java) { t ->
        t.outputFile.set(atlasBuildDir.map { dir -> dir.file("classes.d2") })
      }

      tasks.withType(SvgToPng::class.java) { t ->
        t.outputFile.set(layout.projectDirectory.file("atlas.png"))
      }
    }
}
