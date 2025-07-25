package actual.gradle

import blueprint.core.boolPropertyOrElse
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.compose.reload.gradle.ComposeHotReloadPlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradleSubplugin

class ConventionComposeMp : Plugin<Project> {
  override fun apply(target: Project): Unit = with(target) {
    with(pluginManager) {
      apply(ConventionKotlinBase::class)
      apply(ComposeCompilerGradleSubplugin::class)
      apply(ComposePlugin::class)
      apply(ComposeHotReloadPlugin::class)
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
