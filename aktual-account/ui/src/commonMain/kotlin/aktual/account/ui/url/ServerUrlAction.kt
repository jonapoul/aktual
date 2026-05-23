package aktual.account.ui.url

import aktual.core.model.Protocol
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ServerUrlAction

internal data object ConfirmUrl : ServerUrlAction

internal data object UseDemoServer : ServerUrlAction

internal data object OpenAbout : ServerUrlAction

internal data class EnterUrl(val url: String) : ServerUrlAction

internal data class SelectProtocol(val protocol: Protocol) : ServerUrlAction

@Immutable
internal fun interface ServerUrlActionHandler {
  operator fun invoke(action: ServerUrlAction)
}
