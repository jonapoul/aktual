package aktual.gradle

import aktual.gradle.dsl.apply
import org.gradle.api.Project

class ModuleDi : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(ModuleKotlin::class)
      apply(ConventionDi::class)
    }
  }
}
