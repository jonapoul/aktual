package aktual.budget.model

import androidx.compose.runtime.Immutable

@Immutable
data class Budget(
  val name: String,
  val state: BudgetState,
  val encryptKeyId: String?,
  val groupId: String,
  val cloudFileId: BudgetId,
  val hasKey: Boolean,
)
