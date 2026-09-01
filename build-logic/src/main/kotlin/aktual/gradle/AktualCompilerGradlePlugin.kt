package aktual.gradle

import aktual.compiler.AktualCommandLineProcessor
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class AktualCompilerGradlePlugin : KotlinCompilerPluginSupportPlugin {
  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = true

  override fun applyToCompilation(
    kotlinCompilation: KotlinCompilation<*>
  ): Provider<List<SubpluginOption>> = kotlinCompilation.target.project.provider { emptyList() }

  override fun getCompilerPluginId(): String = AktualCommandLineProcessor.PLUGIN_ID

  override fun getPluginArtifact(): SubpluginArtifact =
    SubpluginArtifact(
      groupId = "aktual.compiler",
      artifactId = "compiler-plugin",
      version = "1.0.0",
    )
}
