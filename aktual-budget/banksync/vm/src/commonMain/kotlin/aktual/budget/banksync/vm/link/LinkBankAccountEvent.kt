package aktual.budget.banksync.vm.link

sealed interface LinkBankAccountEvent {
  data object Linked : LinkBankAccountEvent

  @JvmInline value class LinkFailed(val cause: String?) : LinkBankAccountEvent
}
