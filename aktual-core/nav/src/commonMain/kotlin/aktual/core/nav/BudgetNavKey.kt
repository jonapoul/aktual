package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface BudgetNavKey : NavKey {
  val tab: BudgetTab

  @Immutable
  sealed interface Transactions : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Transactions
  }

  @Immutable
  sealed interface Reports : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Reports
  }

  @Immutable
  sealed interface Rules : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Rules
  }

  @Immutable
  sealed interface Schedules : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Schedules
  }
}
