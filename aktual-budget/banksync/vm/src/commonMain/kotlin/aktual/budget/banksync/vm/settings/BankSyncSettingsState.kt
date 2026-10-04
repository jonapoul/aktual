package aktual.budget.banksync.vm.settings

import aktual.budget.banksync.domain.MappedField
import aktual.budget.banksync.domain.TransactionDirection
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface BankSyncSettingsState {
  data object Loading : BankSyncSettingsState

  @JvmInline value class Failure(val cause: String?) : BankSyncSettingsState

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
