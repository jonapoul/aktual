package aktual.gradle

import dev.zacsweers.metro.gradle.MetroGradleSubplugin
import dev.zacsweers.metro.gradle.MetroPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

class ConventionDi : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(MetroGradleSubplugin::class)
    }

    extensions.configure<MetroPluginExtension> {
      enableKotlinVersionCompatibilityChecks.set(true)
      enableFullBindingGraphValidation.set(true)
      enableTopLevelFunctionInjection.set(false)
      generateAssistedFactories.set(false)
      generateContributionHints.set(true)
      shrinkUnusedBindings.set(true)
      transformProvidersToPrivate.set(true)
      warnOnInjectAnnotationPlacement.set(true)

      reportsDestination.set(layout.buildDirectory.dir("reports/metro"))
    }
  }
}
