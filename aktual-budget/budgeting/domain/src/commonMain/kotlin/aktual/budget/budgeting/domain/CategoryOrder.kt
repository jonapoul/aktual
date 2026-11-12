package aktual.budget.budgeting.domain

import aktual.budget.db.dao.BudgetDao
import aktual.budget.db.dao.DatabaseTables.CATEGORIES
import aktual.budget.db.dao.DatabaseTables.CATEGORY_GROUPS
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.LocalChange
import aktual.budget.model.localChange
import aktual.budget.utils.BudgetSyncController
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding

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

@ContributesBinding(BudgetScope::class)
class CategoryOrderImpl
internal constructor(
  private val budgetDao: BudgetDao,
  private val syncController: BudgetSyncController,
) : CategoryOrder {
  override suspend fun load(): List<OrderedGroup> {
    val categories = budgetDao.categoryOrder().groupBy { it.group_id }
    return budgetDao.categoryGroups().map { group ->
      OrderedGroup(
        id = group.id,
        name = group.name.orEmpty(),
        isIncome = group.is_income == true,
        isHidden = group.hidden,
        categories =
          categories[group.id].orEmpty().map {
            OrderedCategory(id = it.id, name = it.name.orEmpty(), isHidden = it.hidden)
          },
      )
    }
  }

  override suspend fun save(groups: List<OrderedGroup>) {
    val storedGroups = budgetDao.categoryGroups().associate { it.id to it.sort_order }
    val storedCategories = budgetDao.categoryOrder().associateBy { it.id }
    val changes = mutableListOf<LocalChange>()

    // Income groups can't move, so only the expense groups are ordered
    val expenseGroups = groups.mapNotNull { g -> g.id.takeIf { !g.isIncome && it in storedGroups } }
    val groupOrders = sortOrders(expenseGroups.map { it to storedGroups[it] })
    groupOrders.forEach { (id, order) ->
      changes += localChange(CATEGORY_GROUPS, id.value, "sort_order", order)
    }

    for (group in groups) {
      val ids = group.categories.map { it.id }.filter { it in storedCategories }
      val stored = ids.map { id ->
        val row = storedCategories.getValue(id)
        id to row.sort_order.takeIf { row.group_id == group.id }
      }
      for (id in ids) {
        if (storedCategories.getValue(id).group_id != group.id) {
          changes += localChange(CATEGORIES, id.value, "cat_group", group.id.value)
        }
      }
      sortOrders(stored).forEach { (id, order) ->
        changes += localChange(CATEGORIES, id.value, "sort_order", order)
      }
    }

    if (changes.isNotEmpty()) syncController.syncChanges(changes)
  }
}

/**
 * New sort orders for the items whose stored ones don't fit the wanted order, keyed by ID. Items
 * keep their stored order when they're part of the longest run already in order, so moving one item
 * writes only that one. A null stored order is an item that has to move, like one from another
 * group. The others go between their neighbours, or everything is renumbered when there's no room,
 * as shoveSortOrders() does in packages/loot-core/src/server/db/sort.ts.
 */
internal fun <T> sortOrders(items: List<Pair<T, Double?>>): Map<T, Double> {
  val kept = longestIncreasing(items.map { it.second })
  val result = linkedMapOf<T, Double>()
  var lower = 0.0
  var index = 0
  while (index < items.size) {
    if (index in kept) {
      lower = items[index].second ?: lower
      index++
      continue
    }
    val runEnd = (index until items.size).firstOrNull { it in kept } ?: items.size
    val upper = items.getOrNull(runEnd)?.second
    val count = runEnd - index
    val step = if (upper == null) SORT_INCREMENT else (upper - lower) / (count + 1)
    if (step < MIN_GAP) return renumbered(items)
    for (offset in 0 until count) {
      result[items[index + offset].first] = lower + step * (offset + 1)
    }
    lower += step * count
    index = runEnd
  }
  return result
}

private fun <T> renumbered(items: List<Pair<T, Double?>>): Map<T, Double> =
  items
    .mapIndexed { index, (id, stored) -> Triple(id, stored, SORT_INCREMENT * (index + 1)) }
    .filter { (_, stored, order) -> stored != order }
    .associate { (id, _, order) -> id to order }

// The indices of the longest strictly increasing run of non-null values, earliest first on ties
private fun longestIncreasing(values: List<Double?>): Set<Int> {
  val lengths = IntArray(values.size)
  val previous = IntArray(values.size) { -1 }
  var best = -1
  for (i in values.indices) {
    val value = values[i] ?: continue
    lengths[i] = 1
    for (j in 0 until i) {
      val other = values[j] ?: continue
      if (other < value && lengths[j] + 1 > lengths[i]) {
        lengths[i] = lengths[j] + 1
        previous[i] = j
      }
    }
    if (best == -1 || lengths[i] > lengths[best]) best = i
  }
  return generateSequence(best.takeIf { it >= 0 }) { previous[it].takeIf { p -> p >= 0 } }.toSet()
}

private const val SORT_INCREMENT = 16384.0

// Below this, the items are renumbered rather than squeezed between their neighbours
private const val MIN_GAP = 1.0
