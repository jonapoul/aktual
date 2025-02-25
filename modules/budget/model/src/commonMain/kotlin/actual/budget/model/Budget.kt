package actual.budget.model

data class Budget(
  val name: String,
  val state: BudgetState,
  val encryptKeyId: String?,
  val groupId: String,
  val cloudFileId: BudgetId,
) {
  val hasKey = encryptKeyId != null
}
