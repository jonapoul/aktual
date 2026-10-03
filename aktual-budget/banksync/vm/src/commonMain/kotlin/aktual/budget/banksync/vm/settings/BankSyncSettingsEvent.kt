package aktual.budget.banksync.vm.settings

sealed interface BankSyncSettingsEvent {
  data object Saved : BankSyncSettingsEvent

  @JvmInline value class SaveFailed(val cause: String?) : BankSyncSettingsEvent

  data object Unlinked : BankSyncSettingsEvent

  @JvmInline value class UnlinkFailed(val cause: String?) : BankSyncSettingsEvent
}
