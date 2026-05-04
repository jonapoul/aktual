package aktual.gradle

import aktual.gradle.dsl.apply
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionStyle : Plugin<Project> {
  override fun apply(target: Project) =
    with(target.pluginManager) {
      apply(ConventionDetekt::class)
      apply(ConventionLicensee::class)
    }
}
