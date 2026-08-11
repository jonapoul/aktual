package aktual.gradle

import aktual.gradle.dsl.apply
import org.gradle.api.Project

class ConventionStyle : ProjectPlugin {
  override fun Project.applyTo() {
    with(pluginManager) {
      apply(ConventionDetekt::class)
      apply(ConventionLicensee::class)
    }
  }
}
