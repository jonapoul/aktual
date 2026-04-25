package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class BudgetUserWithAccess(
  val userId: String,
  val userName: String,
  val displayName: String,
  val isOwner: Boolean,
)
