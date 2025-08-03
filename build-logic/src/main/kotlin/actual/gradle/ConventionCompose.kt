package actual.gradle

import blueprint.core.boolPropertyOrElse
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin

class ConventionCompose : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinBase::class.java)
      apply(ConventionAndroidBase::class.java)
      apply(ComposeCompilerGradleSubplugin::class)
      apply(ComposePlugin::class)
    }

    addAndroidFlag()
    addMetrics()
    addLint()
    addComposeBom()

    if (boolPropertyOrElse(key = "actual.compose.android", default = true)) {
      addComposeAndroid()
    }
  }
}
