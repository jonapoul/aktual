package aktual.budget.model

data class BudgetUserWithAccess(
  val userId: String,
  val userName: String,
  val displayName: String,
  val isOwner: Boolean,
)
