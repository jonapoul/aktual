package aktual.budget.accounts.vm.edit

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.model.AccountGroupId
import aktual.budget.model.Amount
import androidx.compose.runtime.Immutable
import kotlin.jvm.JvmInline
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface EditAccountState

data object Loading : EditAccountState

@JvmInline value class Failure(val message: String) : EditAccountState

// The working edits, seeded from the account when it first loads
data class Editing(
  val name: String,
  val notes: String,
  val group: AccountGroupId?,
  val groups: ImmutableList<AccountGroupItem>,
  val offBudget: Boolean,
  val balance: Amount,
  val transactionCount: Long,
  // The trimmed name when another account already has it
  val duplicateName: String?,
  val canSave: Boolean,
  val isSaving: Boolean,
  val saveError: String?,
) : EditAccountState
