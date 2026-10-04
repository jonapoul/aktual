package aktual.budget.banksync.vm.settings

import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.domain.TransactionDirection
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface BankSyncSettingsState {
  data object Loading : BankSyncSettingsState

  @JvmInline value class Failure(val cause: String?) : BankSyncSettingsState

  /**
   * @property importTransactions Off for an investment account, which only syncs its balance. The
   *   other toggles do nothing then.
   * @property fields How [direction]'s fields are mapped, or null if the account has no synced
   *   transaction in that direction to offer fields to map from.
   * @property hasChanges The settings differ from the saved ones.
   */
  @Immutable
  data class Editing(
    val accountName: String?,
    val importTransactions: Boolean,
    val importPending: Boolean,
    val importNotes: Boolean,
    val reimportDeleted: Boolean,
    val updateDates: Boolean,
    val direction: TransactionDirection,
    val fields: ImmutableList<MappedFieldRow>?,
    val hasChanges: Boolean,
  ) : BankSyncSettingsState
}

/** Which downloaded field fills in [field], out of the [options] the example transaction has. */
@Immutable
data class MappedFieldRow(
  val field: MappedField,
  val selected: String?,
  val options: ImmutableList<FieldOption>,
) {
  val example: String?
    get() = options.firstOrNull { it.field == selected }?.example
}

@Immutable data class FieldOption(val field: String, val example: String)

enum class BankSyncToggle {
  ImportTransactions,
  ImportPending,
  ImportNotes,
  ReimportDeleted,
  UpdateDates,
}
