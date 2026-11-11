package aktual.budget.accounts.vm

import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.model.AccountGroupId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class CreateAccountState(
  val offBudget: Boolean = false,
  // Null is Ungrouped
  val group: AccountGroupId? = null,
  val groups: ImmutableList<AccountGroupItem> = persistentListOf(),
  val isNegative: Boolean = false,
  // The trimmed name when another account already has it
  val duplicateName: String? = null,
  val isBalanceValid: Boolean = true,
  val canCreate: Boolean = false,
  val isSaving: Boolean = false,
  val saveError: String? = null,
)
