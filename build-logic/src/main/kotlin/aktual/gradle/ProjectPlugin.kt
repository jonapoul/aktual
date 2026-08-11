package aktual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

interface ProjectPlugin : Plugin<Project> {
  fun Project.applyTo()

  override fun apply(target: Project) = target.applyTo()
}
