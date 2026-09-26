package aktual.budget.navrail.vm

import androidx.compose.runtime.Immutable

@Immutable
data class DrawerHeaderState(
  val budgetName: String?,
  val serverHost: String,
)
