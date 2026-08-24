package aktual.compiler

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

@OptIn(ExperimentalCompilerApi::class)
class AktualCompilerPluginRegistrar : CompilerPluginRegistrar() {
  override val pluginId: String = AktualCommandLineProcessor.PLUGIN_ID
  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    // Intentionally no-op: registers no compiler extensions.
  }
}
