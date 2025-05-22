package actual.url.ui

import actual.core.model.Protocol
import androidx.compose.runtime.Immutable
import dev.drewhamilton.poko.Poko

@Immutable
sealed interface ServerUrlAction {
  data object NavBack : ServerUrlAction
  data object ConfirmUrl : ServerUrlAction
  data object UseDemoServer : ServerUrlAction

  @Poko class EnterUrl(val url: String) : ServerUrlAction
  @Poko class SelectProtocol(val protocol: Protocol) : ServerUrlAction
}
