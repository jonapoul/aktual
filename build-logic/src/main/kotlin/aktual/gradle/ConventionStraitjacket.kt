package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import org.gradle.api.Plugin
import org.gradle.api.Project
import straitjacket.StraitjacketExtension
import straitjacket.StraitjacketPlugin

class ConventionStraitjacket : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply(StraitjacketPlugin::class)

      extensions.configure(StraitjacketExtension::class) {
        // TBC
      }
    }
}
