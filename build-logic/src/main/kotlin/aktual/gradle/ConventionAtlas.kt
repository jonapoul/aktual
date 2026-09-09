package aktual.gradle

import atlas.d2.tasks.ExecD2
import atlas.d2.tasks.WriteD2Chart
import atlas.d2.tasks.WriteD2Classes
import org.gradle.api.Project

/** Put atlas PNGs in the root of the module, not in `module/atlas/chart.png`. */
class ConventionAtlas : ProjectPlugin {
  override fun Project.applyTo() {
    val atlasBuildDir = layout.buildDirectory.dir("atlas")
    val projectDir = layout.projectDirectory

    tasks.withType(WriteD2Chart::class.java) { t ->
      t.outputFile.set(atlasBuildDir.map { d -> d.file("chart.d2") })
    }

    tasks.withType(ExecD2::class.java) { t ->
      t.outputFile.set(t.outputFormat.map { format -> projectDir.file("chart.${format.string}") })
    }

    tasks.withType(WriteD2Classes::class.java) { t ->
      t.outputFile.set(atlasBuildDir.map { dir -> dir.file("classes.d2") })
    }
  }
}
