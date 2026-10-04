package aktual.budget.banksync.ui

import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.domain.TransactionDirection
import aktual.budget.banksync.vm.settings.BankSyncToggle
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface BankSyncSettingsAction

internal data object NavigateBack : BankSyncSettingsAction

internal data object SaveSettings : BankSyncSettingsAction

internal data object UnlinkAccount : BankSyncSettingsAction

internal data class SetToggle(val toggle: BankSyncToggle, val value: Boolean) :
  BankSyncSettingsAction

@JvmInline
internal value class SetDirection(val direction: TransactionDirection) : BankSyncSettingsAction

internal data class SetMapping(val field: MappedField, val value: String) : BankSyncSettingsAction

@Immutable
internal fun interface BankSyncSettingsActionHandler {
  operator fun invoke(action: BankSyncSettingsAction)
}
