package aktual.core.nav

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

enum class BudgetTab(val category: Category) {
  Transactions(Category.Tab),
  Reports(Category.Tab),
  Schedules(Category.Tab),
  Rules(Category.Tab),
  Tags(Category.Tab),
  SwitchBudget(Category.Action),
  LogOut(Category.Action),
  Settings(Category.Action),
  About(Category.Action);

  enum class Category {
    // A primary nav destination with its own back stack, shown in the collapsed nav bar
    Tab,
    // A one-shot action (e.g. log out) that only appears in the expanded grid
    Action,
  }

  companion object {
    val tabs: ImmutableList<BudgetTab> =
      entries.filter { it.category == Category.Tab }.toImmutableList()
  }
}
