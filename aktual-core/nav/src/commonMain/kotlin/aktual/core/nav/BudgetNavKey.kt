package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface BudgetNavKey : NavKey {
  val tab: BudgetTab

  sealed interface Transactions : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Transactions
  }

  sealed interface Reports : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Reports
  }

  sealed interface Rules : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Rules
  }

  sealed interface Schedules : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Schedules
  }

  sealed interface Tags : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Tags
  }
}
