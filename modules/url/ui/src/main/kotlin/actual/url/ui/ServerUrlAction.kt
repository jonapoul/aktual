package actual.url.ui

import actual.url.model.Protocol
import androidx.compose.runtime.Immutable

@Immutable
sealed interface ServerUrlAction {
  data object NavBack : ServerUrlAction
  data object ConfirmUrl : ServerUrlAction

  data class EnterUrl(val url: String) : ServerUrlAction
  data class SelectProtocol(val protocol: Protocol) : ServerUrlAction
}
