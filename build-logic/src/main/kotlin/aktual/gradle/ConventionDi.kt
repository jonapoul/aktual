package aktual.gradle

import aktual.gradle.dsl.apply
import aktual.gradle.dsl.configure
import dev.zacsweers.metro.gradle.MetroGradleSubplugin
import dev.zacsweers.metro.gradle.MetroPluginExtension
import dev.zacsweers.metro.gradle.RequiresIdeSupport
import org.gradle.api.Plugin
import org.gradle.api.Project

class ConventionDi : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      pluginManager.apply(MetroGradleSubplugin::class)

      extensions.configure(MetroPluginExtension::class) {
        enableKotlinVersionCompatibilityChecks.set(true)
        @OptIn(RequiresIdeSupport::class) generateAssistedFactories.set(true)
        generateContributionHints.set(true)
        warnOnInjectAnnotationPlacement.set(true)
      }
    }
}
