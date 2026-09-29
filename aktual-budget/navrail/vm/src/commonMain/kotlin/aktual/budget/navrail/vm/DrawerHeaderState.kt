package aktual.budget.navrail.vm

import androidx.compose.runtime.Immutable

@Immutable
data class DrawerHeaderState(
  val budgetName: String?,
  // Null when the budget has no server, i.e. the demo budget
  val serverHost: String?,
) {
  val isDemo: Boolean
    get() = serverHost == null
}
