package aktual.core.nav

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

enum class BudgetTab(val category: Category) {
  Transactions(Tab),
  Reports(Tab),
  Schedules(Tab),
  Rules(Tab),
  Tags(Tab),
  SwitchBudget(Action),
  LogOut(Action),
  Settings(Action),
  About(Action);

  enum class Category {
    // A primary nav destination with its own back stack, shown in the collapsed nav bar
    Tab,
    // A one-shot action (e.g. log out) that only appears in the expanded grid
    Action,
  }

  companion object {
    val tabs: ImmutableList<BudgetTab> = entries.filter { it.category == Tab }.toImmutableList()
  }
}
