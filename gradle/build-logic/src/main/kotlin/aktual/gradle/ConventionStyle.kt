package aktual.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

class ConventionStyle : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionDetekt::class)
      apply(ConventionLicensee::class)
      apply(ConventionSpotless::class)
    }
  }
}
