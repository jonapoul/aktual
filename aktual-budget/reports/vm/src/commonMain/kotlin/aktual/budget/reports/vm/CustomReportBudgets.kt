package aktual.budget.reports.vm

import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportBudgets
import aktual.budget.model.AccountId
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.Field
import aktual.budget.model.Interval
import aktual.budget.rules.domain.likePatternToRegex
import aktual.budget.rules.domain.normalise
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

// Upstream's sidebar only offers budgeted amounts grouped by category or group, and not daily or
// weekly. Budgets have no payee or account and are keyed by month, so anything else comes out empty
internal fun CustomReportConfig.canShowBudgeted(): Boolean =
  groupBy != Payee &&
    groupBy != Account &&
    groupBy != Tag &&
    interval in setOf<Interval>(Monthly, Quarterly, Yearly)

// packages/desktop-client/src/components/reports/spreadsheets/budgetDataQuery.ts fetchBudgetData():
// each budget as the amounts row a transaction would make, positive budgets as assets and negative
// ones as debts. Budgets have no account, so they're all summed under an empty one as upstream does
internal fun budgetAmounts(
  rows: List<CustomReportBudgets>,
  conditions: List<Condition>,
  conditionsOp: ConditionOp,
): List<CustomReportAmounts> {
  val filter = BudgetCategoryFilter(conditions, conditionsOp)
  return rows
    .filter { row ->
      filter.matches(row.category, row.category_name, row.category_group, row.category_group_name)
    }
    .map { row ->
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
// category and category group conditions count. If any of those can't be read, nothing is filtered.
// A null category is the uncategorised row, which only matches conditions on a null ID
internal class BudgetCategoryFilter(conditions: List<Condition>, private val op: ConditionOp) {
  private val conditions: List<Condition>? =
    conditions
      .filter { it.customName == null && it.field in FIELDS }
      .takeIf { list -> list.all { it.isSupported() } }

  private val likes: Map<Condition, Regex> =
    this.conditions
      .orEmpty()
      .filter { it.operator == Contains || it.operator == DoesNotContain }
      .associateWith { likePatternToRegex(normalise("%${it.value.string()}%")) }

  fun matches(
    category: CategoryId?,
    categoryName: String?,
    group: CategoryGroupId?,
    groupName: String?,
  ): Boolean {
    if (conditions.isNullOrEmpty()) return true
    val target = Target(category, categoryName, group, groupName)
    return if (op == Or) {
      conditions.any { it.matches(target) }
    } else {
      conditions.all { it.matches(target) }
    }
  }

  private fun Condition.isSupported(): Boolean =
    when (operator) {
      Is,
      IsNot -> value is JsonNull || value.string() != null
      Contains,
      DoesNotContain,
      Matches -> value.string() != null
      OneOf,
      NotOneOf -> value.ids() != null
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

  private fun Condition.matches(target: Target): Boolean {
    val isGroup = field == CategoryGroup
    val key = if (isGroup) target.group?.value else target.category?.value
    val text =
      when {
        isGroup -> target.groupName ?: key
        target.category == null -> null
        else -> target.categoryName.orEmpty()
      }
    val string = value.string()
    return when (operator) {
      Is -> key == string
      IsNot -> key != string
      OneOf -> value.ids().orEmpty().contains(key)
      NotOneOf -> value.ids().orEmpty().let { it.isNotEmpty() && key !in it }
      Contains -> isLike(text)
      DoesNotContain -> text == null || !isLike(text)
      Matches ->
        string.orEmpty().let { it.length > MAX_PATTERN || text.orEmpty().matchesPattern(it) }
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

  private fun Condition.isLike(text: String?): Boolean =
    likes.getValue(this).containsMatchIn(text?.let(::normalise).orEmpty())

  private fun String.matchesPattern(pattern: String): Boolean = runCatching {
    Regex(pattern).containsMatchIn(this)
  }
    .getOrDefault(false)

  private fun JsonElement.string(): String? =
    (this as? JsonPrimitive)?.takeIf { it.isString }?.content

  private fun JsonElement.ids(): List<String?>? =
    (this as? JsonArray)?.map { if (it is JsonNull) null else it.string() ?: return null }

  private data class Target(
    val category: CategoryId?,
    val categoryName: String?,
    val group: CategoryGroupId?,
    val groupName: String?,
  )

  private companion object {
    val FIELDS = setOf<Field>(Category, CategoryGroup)

    // Upstream skips longer patterns, so they match everything
    const val MAX_PATTERN = 256
  }
}
