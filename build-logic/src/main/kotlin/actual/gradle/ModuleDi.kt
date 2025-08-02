package actual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleDi : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ModuleMultiplatform::class.java)
      apply(ConventionDi::class.java)
    }
  }
}
