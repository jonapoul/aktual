package aktual.budget.budgeting.domain

import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId

data class OrderedGroup(
  val id: CategoryGroupId,
  val name: String,
  val isIncome: Boolean,
  val isHidden: Boolean,
  val categories: List<OrderedCategory>,
)

data class OrderedCategory(val id: CategoryId, val name: String, val isHidden: Boolean)

// The order of groups and categories on the budget page, as moveCategory() and
// moveCategoryGroup() in packages/loot-core/src/server/db/index.ts write it
interface CategoryOrder {
  // Expense groups first, then income ones, hidden ones included
  suspend fun load(): List<OrderedGroup>

  // Writes the sort orders and groups that differ from the stored ones, in one batch
  suspend fun save(groups: List<OrderedGroup>)
}
