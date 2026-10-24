package aktual.budget.accounts.vm

import androidx.compose.runtime.Immutable

@Immutable
data class CreateAccountState(
  val offBudget: Boolean = false,
  val isNegative: Boolean = false,
  // The trimmed name when another account already has it
  val duplicateName: String? = null,
  val isBalanceValid: Boolean = true,
  val canCreate: Boolean = false,
  val isSaving: Boolean = false,
  val saveError: String? = null,
)
