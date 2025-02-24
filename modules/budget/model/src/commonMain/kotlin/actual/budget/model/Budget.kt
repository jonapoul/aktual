package actual.budget.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class Budget(
  val name: String,
  val state: BudgetState,
  val encryptKeyId: String?,
  val groupId: String,
  val cloudFileId: BudgetId,
)

@get:Stable
val Budget.hasKey: Boolean
  get() = encryptKeyId != null
