package aktual.account.vm

import androidx.compose.runtime.Immutable

@Immutable
sealed interface LoginEvent {
  data object Timeout : LoginEvent

  @JvmInline value class Redirect(val url: String) : LoginEvent
}
