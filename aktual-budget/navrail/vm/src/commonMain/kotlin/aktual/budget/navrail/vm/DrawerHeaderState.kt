package aktual.budget.navrail.vm

import androidx.compose.runtime.Immutable

@Immutable
data class DrawerHeaderState(
  val budgetName: String?,
  val serverHost: String?,
  val canManageApiKeys: Boolean = false,
) {
  val isDemo: Boolean
    get() = serverHost == null
}
