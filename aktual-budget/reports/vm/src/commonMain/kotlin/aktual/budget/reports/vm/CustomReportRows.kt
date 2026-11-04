package aktual.budget.reports.vm

import aktual.budget.db.CustomReportCategories
import aktual.budget.db.CustomReportCategoryGroups
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull

// packages/desktop-client/src/components/reports/spreadsheets/reportCategoryLists.ts: which
// category
// and group rows the report can show, whatever their activity. Selected categories with nothing in
// them stay as empty rows, but showing empty rows never brings back the ones filtered out
internal class CustomReportRows(
  config: CustomReportConfig,
  categories: List<CustomReportCategories>,
  groups: List<CustomReportCategoryGroups>,
) {
  private val showUncategorized = config.showUncategorized
  private val showOffBudget = config.showOffBudget
  private val categoryIds: Set<CategoryId>
  private val groupIds: Set<CategoryGroupId>
  private val allowsUncategorized: Boolean
  private val allowsTransfers: Boolean

  init {
    val plain = config.conditions.filter { it.customName == null }
    // With Or, a condition on another field can match transactions in any category, so the
    // category conditions can't rule out a row
    val conditions =
      if (
        config.conditionsOp == Or && plain.any { it.field != Category && it.field != CategoryGroup }
      ) {
        emptyList()
      } else {
        plain
      }
    val filter = BudgetCategoryFilter(conditions, config.conditionsOp)
    val groupNames = groups.associate { it.id to it.name }
    val eligible = categories.filter { category ->
      (config.showHidden || !(category.hidden || category.group_hidden == true)) &&
        filter.matches(
          category.id,
          category.name,
          category.cat_group,
          category.cat_group?.let(groupNames::get),
        )
    }
    categoryIds = eligible.mapTo(HashSet()) { it.id }
    groupIds = eligible.mapNotNullTo(HashSet()) { it.cat_group }
    allowsUncategorized = filter.matchesUncategorized()
    // conditionsToAQL expands "category is null" to also leave out transfers
    allowsTransfers =
      BudgetCategoryFilter(conditions.map { it.withoutTransfers() }, config.conditionsOp)
        .matchesUncategorized()
  }

  fun isEligible(group: CustomGroup): Boolean =
    when (group) {
      is Category -> group.id in categoryIds
      is Group -> group.id in groupIds
      Uncategorized -> showUncategorized && allowsUncategorized
      Transfers -> showUncategorized && allowsTransfers
      // Off budget transactions may have a selected category, or none
      OffBudget -> showOffBudget && (categoryIds.isNotEmpty() || allowsUncategorized)
      UncategorizedAndOffBudget ->
        isEligible(Uncategorized) || isEligible(Transfers) || isEligible(OffBudget)
      is Account,
      is Payee,
      is TagSet,
      Untagged -> true
    }

  private fun BudgetCategoryFilter.matchesUncategorized() = matches(null, null, null, null)

  private fun Condition.withoutTransfers(): Condition =
    if (field == Category && operator == Is && value is JsonNull) {
      copy(operator = OneOf, value = JsonArray(emptyList()))
    } else {
      this
    }
}
