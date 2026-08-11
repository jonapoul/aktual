package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import org.gradle.api.Project
import straitjacket.StraitjacketExtension
import straitjacket.StraitjacketPlugin

class ConventionStraitjacket : ProjectPlugin {
  override fun Project.applyTo() {
    pluginManager.apply(StraitjacketPlugin::class)

    extensions.configure(StraitjacketExtension::class) {
      // TBC
    }
  }
}
