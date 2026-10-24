package aktual.budget.transactions.vm.edit

import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.budget.db.GetAllGrouped
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import logcat.logcat

// How the category picker shows the categories
internal data class CategoryView(val showHidden: Boolean, val balances: Map<CategoryId, Amount>)

// Every category under its group, hidden ones included
internal fun List<GetAllGrouped>.toCategoryGroups(): List<CategoryGroupOptions> = groupBy {
  it.groupId
}
  .values
  .map { rows ->
    val groupHidden = rows.first().groupHidden == true
    CategoryGroupOptions(
      name = rows.first().groupName,
      categories =
        rows
          .mapNotNull { row ->
            row.name?.let { name ->
              CategoryOption(row.id, name, isHidden = row.hidden || groupHidden)
            }
          }
          .toImmutableList(),
      isHidden = groupHidden,
      id = rows.first().groupId,
    )
  }

// Leaves out the hidden categories unless they're shown, and adds the balances
internal fun TransactionOptions.withCategories(view: CategoryView): TransactionOptions {
  val groups = categoryGroups.mapNotNull { group ->
    val categories =
      group.categories
        .filter { view.showHidden || !it.isHidden }
        .map { it.copy(balance = view.balances[it.id]) }
    if (categories.isEmpty()) null else group.copy(categories = categories.toImmutableList())
  }
  return copy(categoryGroups = groups.toImmutableList(), showHiddenCategories = view.showHidden)
}

// What's left in each category in the month. As upstream's category picker, income categories have
// no balance
internal fun BudgetMonthCalculator.observeBalances(
  month: YearMonth?,
): Flow<Map<CategoryId, Amount>> {
  if (month == null) return flowOf(emptyMap())
  return observe(month)
    .map { budget -> budget.categories.filter { !it.isIncome }.associate { it.id to it.balance } }
    .distinctUntilChanged()
    .catch { e ->
      logcat.w("BudgetMonthCalculator", e) { "Failed loading category balances for $month" }
      emit(emptyMap())
    }
}
