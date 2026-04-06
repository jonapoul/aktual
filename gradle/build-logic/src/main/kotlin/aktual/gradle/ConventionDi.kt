package aktual.gradle

import dev.zacsweers.metro.gradle.DelicateMetroGradleApi
import dev.zacsweers.metro.gradle.MetroGradleSubplugin
import dev.zacsweers.metro.gradle.MetroPluginExtension
import dev.zacsweers.metro.gradle.RequiresIdeSupport
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ConventionDi : Plugin<Project> {
  override fun apply(target: Project) =
    with(target) {
      with(pluginManager) { apply(MetroGradleSubplugin::class) }

      extensions.configure<MetroPluginExtension> {
        enableKotlinVersionCompatibilityChecks.set(true)
        @OptIn(DelicateMetroGradleApi::class) enableFullBindingGraphValidation.set(true)
        @OptIn(RequiresIdeSupport::class) generateAssistedFactories.set(true)
        generateContributionHints.set(true)
        @OptIn(DelicateMetroGradleApi::class) shrinkUnusedBindings.set(true)
        warnOnInjectAnnotationPlacement.set(true)
      }
    }
}
