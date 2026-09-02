package aktual.compiler.id

import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class IdFirExtensionRegistrar : FirExtensionRegistrar() {
  override fun ExtensionRegistrarContext.configurePlugin() {
    +::IdFirSupertypeGenerationExtension
    +::IdFirDeclarationGenerationExtension
    +::IdFirCheckersExtension
  }
}
