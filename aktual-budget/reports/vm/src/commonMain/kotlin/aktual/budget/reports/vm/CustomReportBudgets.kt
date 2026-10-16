package aktual.budget.reports.vm

import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportBudgets
import aktual.budget.model.AccountId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

// Upstream's sidebar only offers budgeted amounts grouped by category or group, monthly or yearly.
// Budgets have no payee or account, and are keyed by month, so anything else would come out empty
internal fun CustomReportConfig.canShowBudgeted(): Boolean =
  groupBy != Payee && groupBy != Account && (interval == Monthly || interval == Yearly)

// packages/desktop-client/src/components/reports/spreadsheets/budgetDataQuery.ts fetchBudgetData():
// each budget as the amounts row a transaction would make, positive budgets as assets and negative
// ones as debts. Budgets have no account, so they're all summed under an empty one as upstream does
internal fun budgetAmounts(
  rows: List<CustomReportBudgets>,
  conditions: List<Condition>,
  conditionsOp: ConditionOp,
): List<CustomReportAmounts> {
  val filter = BudgetCategoryFilter(conditions, conditionsOp)
  return rows.filter(filter::matches).map { row ->
    val amount = row.amount ?: 0L
    CustomReportAmounts(
      interval_key = row.interval_key,
      category = row.category,
      category_hidden = row.category_hidden,
      category_group = row.category_group,
      category_group_hidden = row.category_group_hidden,
      account = NO_ACCOUNT,
      off_budget = false,
      payee = null,
      transfer_account = null,
      assets = amount.coerceAtLeast(0L),
      debts = amount.coerceAtMost(0L),
    )
  }
}

private val NO_ACCOUNT = AccountId("")

// budgetDataQuery.ts filterCategoriesByConditions(): budgets only have a category to match, so only
// category and category group conditions count. If any of those can't be read, nothing is filtered
private class BudgetCategoryFilter(conditions: List<Condition>, private val op: ConditionOp) {
  private val conditions: List<Condition>? =
    conditions
      .filter { it.customName == null && it.field in FIELDS }
      .takeIf { list -> list.all { it.isSupported() } }

  fun matches(row: CustomReportBudgets): Boolean {
    if (conditions.isNullOrEmpty()) return true
    return if (op == Or) {
      conditions.any { it.matches(row) }
    } else {
      conditions.all { it.matches(row) }
    }
  }

  private fun Condition.isSupported(): Boolean =
    when (operator) {
      Is,
      IsNot,
      Contains,
      DoesNotContain,
      Matches -> value.string() != null
      OneOf,
      NotOneOf -> value.strings() != null
      GreaterThan,
      GreaterThanOrEquals,
      HasAnyTag,
      HasTags,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals,
      OffBudget,
      OnBudget -> false
    }

  private fun Condition.matches(row: CustomReportBudgets): Boolean {
    val isGroup = field == CategoryGroup
    val key = if (isGroup) row.category_group?.value.orEmpty() else row.category.value
    val text = if (isGroup) row.category_group_name ?: key else row.category_name.orEmpty()
    val string = value.string()
    return when (operator) {
      Is -> key == string
      IsNot -> key != string
      OneOf -> value.strings().orEmpty().contains(key)
      NotOneOf -> !value.strings().orEmpty().contains(key)
      Contains -> text.contains(string.orEmpty(), ignoreCase = true)
      DoesNotContain -> !text.contains(string.orEmpty(), ignoreCase = true)
      Matches -> string.orEmpty().let { it.length > MAX_PATTERN || text.matchesPattern(it) }
      GreaterThan,
      GreaterThanOrEquals,
      HasAnyTag,
      HasTags,
      IsApprox,
      IsBetween,
      LessThan,
      LessThanOrEquals,
      OffBudget,
      OnBudget -> true
    }
  }

  private fun String.matchesPattern(pattern: String): Boolean = runCatching {
    Regex(pattern, RegexOption.IGNORE_CASE).containsMatchIn(this)
  }
    .getOrDefault(false)

  private fun JsonElement.string(): String? =
    (this as? JsonPrimitive)?.takeIf { it.isString }?.content

  private fun JsonElement.strings(): List<String>? =
    (this as? JsonArray)?.map { it.string() ?: return null }

  private companion object {
    val FIELDS = setOf(Field.Category, Field.CategoryGroup)

    // Upstream skips longer patterns, so they match everything
    const val MAX_PATTERN = 256
  }
}
