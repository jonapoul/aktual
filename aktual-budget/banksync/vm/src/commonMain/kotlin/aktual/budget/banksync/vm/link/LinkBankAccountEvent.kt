package aktual.budget.banksync.vm.link

sealed interface LinkBankAccountEvent {
  data object Linked : LinkBankAccountEvent

  @JvmInline value class LinkFailed(val cause: String?) : LinkBankAccountEvent

  /** Open [url] in a browser, for the user to log in to their bank. */
  @JvmInline value class OpenBrowser(val url: String) : LinkBankAccountEvent
}
