package aktual.account.ui.url

import aktual.core.model.Protocol
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ServerUrlAction {
  data object NavBack : ServerUrlAction

  data object ConfirmUrl : ServerUrlAction

  data object UseDemoServer : ServerUrlAction

  data object OpenAbout : ServerUrlAction

  data class EnterUrl(val url: String) : ServerUrlAction

  data class SelectProtocol(val protocol: Protocol) : ServerUrlAction
}
