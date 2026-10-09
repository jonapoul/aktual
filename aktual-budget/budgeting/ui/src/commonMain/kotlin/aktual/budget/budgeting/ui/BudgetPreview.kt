@file:Suppress("MagicNumber")

package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.Banner
import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.BudgetSummary
import aktual.budget.budgeting.vm.CategoryRow
import aktual.budget.budgeting.vm.GroupRow
import aktual.budget.model.Amount
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

private fun category(
  name: String,
  budgeted: Double,
  spent: Double,
  balance: Double,
  carryover: Boolean = false,
  isHidden: Boolean = false,
) =
  CategoryRow(
    id = CategoryId(name),
    name = name,
    isHidden = isHidden,
    budgeted = Amount(budgeted),
    spent = Amount(spent),
    balance = Amount(balance),
    carryover = carryover,
  )

private fun group(
  name: String,
  vararg categories: CategoryRow,
  isCollapsed: Boolean = false,
  isHidden: Boolean = false,
) =
  GroupRow(
    id = CategoryGroupId(name),
    name = name,
    isHidden = isHidden,
    isCollapsed = isCollapsed,
    budgeted = categories.fold(Amount.Zero) { sum, c -> sum + c.budgeted },
    spent = categories.fold(Amount.Zero) { sum, c -> sum + c.spent },
    balance = categories.fold(Amount.Zero) { sum, c -> sum + c.balance },
    categories = categories.toList().toImmutableList(),
  )

private val PREVIEW_GROUPS =
  persistentListOf(
    group(
      "Bills",
      category("Rent", budgeted = 900.0, spent = -900.0, balance = 0.0),
      category("Utilities", budgeted = 100.0, spent = -60.0, balance = 40.0),
      category("Phone", budgeted = 50.0, spent = -50.0, balance = 0.0),
    ),
    group(
      "Food",
      category("Groceries", budgeted = 350.0, spent = -325.0, balance = 25.0),
      category("Eating out", budgeted = 100.0, spent = -140.0, balance = -40.0),
      category("Takeaway", budgeted = 0.0, spent = 0.0, balance = 0.0, isHidden = true),
    ),
    group(
      "Transport",
      category("Fuel", budgeted = 120.0, spent = -108.0, balance = 12.0),
      category("Parking", budgeted = 40.0, spent = -20.0, balance = 20.0, carryover = true),
    ),
    group(
      "Savings",
      category("Emergency fund", budgeted = 300.0, spent = 0.0, balance = 900.0, carryover = true),
      isCollapsed = true,
    ),
  )

private val PREVIEW_INCOME =
  group(
    "Income",
    category("Salary", budgeted = 2_500.0, spent = 2_500.0, balance = 0.0),
    category("Side work", budgeted = 200.0, spent = 180.0, balance = 0.0),
  )

internal val PREVIEW_ENVELOPE =
  BudgetState.Loaded(
    type = Envelope,
    month = YearMonth(2026, 10),
    summary =
      BudgetSummary.Envelope(
        toBudget = Amount(620.0),
        available = Amount(2_680.0),
        budgeted = Amount(2_060.0),
      ),
    groups = PREVIEW_GROUPS,
    income = PREVIEW_INCOME,
    banners =
      persistentListOf(
        Banner.Uncategorised(count = 3),
        Banner.Overspent(count = 1, total = Amount(-40.0)),
      ),
    showSpent = false,
    showHidden = true,
  )

internal val PREVIEW_TRACKING =
  PREVIEW_ENVELOPE.copy(
    type = Tracking,
    summary =
      BudgetSummary.Tracking(
        saved = Amount(640.0),
        isProjected = true,
        budgeted = Amount(2_060.0),
        spent = Amount(-1_603.0),
      ),
    banners = persistentListOf(),
    showSpent = true,
    showHidden = false,
  )

internal val PREVIEW_EMPTY =
  PREVIEW_ENVELOPE.copy(groups = persistentListOf(), income = null, banners = persistentListOf())
