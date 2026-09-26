package aktual.gradle

import atlas.d2.tasks.ExecD2
import org.gradle.api.Project

/** Put atlas PNGs in the root of the module, not in `module/atlas/chart.png`. */
class ConventionAtlas : ProjectPlugin {
  override fun Project.applyTo() {
    val projectDir = layout.projectDirectory

    tasks.withType(ExecD2::class.java).configureEach { t ->
      t.outputFile.set(t.outputFormat.map { format -> projectDir.file("chart.$format") })
    }
  }
}
