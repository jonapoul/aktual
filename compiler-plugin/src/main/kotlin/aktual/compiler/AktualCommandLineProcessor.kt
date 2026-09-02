package aktual.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

class AktualCommandLineProcessor : CommandLineProcessor {
  override val pluginId: String = PLUGIN_ID
  override val pluginOptions: Collection<AbstractCliOption> = emptyList()

  override fun processOption(
    option: AbstractCliOption,
    value: String,
    configuration: CompilerConfiguration,
  ) {
    // No options are declared, so this is never called.
  }

  companion object {
    const val PLUGIN_ID = "aktual.compiler"
  }
}
