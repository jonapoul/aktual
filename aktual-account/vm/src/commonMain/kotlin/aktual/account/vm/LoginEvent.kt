package aktual.account.vm

sealed interface LoginEvent {
  data object Timeout : LoginEvent

  @JvmInline value class Redirect(val url: String) : LoginEvent
}
