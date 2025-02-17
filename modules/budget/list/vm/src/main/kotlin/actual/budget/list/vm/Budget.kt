package actual.budget.list.vm

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class Budget(
  val name: String,
  val state: BudgetState,
  val encryptKeyId: String?,
  val groupId: String,
  val cloudFileId: String,
)

@get:Stable
val Budget.hasKey: Boolean
  get() = encryptKeyId != null
