package aktual.core.nav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Immutable
@Serializable
sealed interface BudgetNavKey : NavKey {
  // The tab this route's screen belongs to. It can still be pushed onto another tab's stack
  val tab: BudgetTab

  sealed interface Home : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Home
  }

  sealed interface Budget : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.Budget
  }

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

  sealed interface BankSync : BudgetNavKey {
    override val tab: BudgetTab
      get() = BudgetTab.BankSync
  }
}
