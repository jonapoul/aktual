package aktual.compiler

import aktual.compiler.AktualCommandLineProcessor.Companion.PLUGIN_ID
import aktual.compiler.id.IdFirExtensionRegistrar
import aktual.compiler.id.IdIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter

class AktualCompilerPluginRegistrar : CompilerPluginRegistrar() {
  override val pluginId: String = PLUGIN_ID
  override val supportsK2: Boolean = true

  override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
    FirExtensionRegistrarAdapter.registerExtension(IdFirExtensionRegistrar())
    IrGenerationExtension.registerExtension(IdIrGenerationExtension())
  }
}
