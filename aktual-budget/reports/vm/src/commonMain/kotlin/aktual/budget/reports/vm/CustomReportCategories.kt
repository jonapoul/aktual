package aktual.budget.reports.vm

import aktual.budget.db.BudgetCategories
import aktual.budget.db.BudgetCategoryGroups
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.Operator
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Immutable data class CategoryChoice(val id: CategoryId, val name: String, val isHidden: Boolean)

@Immutable
data class CategoryGroupChoice(
  val id: CategoryGroupId,
  val name: String,
  val isHidden: Boolean,
  val categories: ImmutableList<CategoryChoice>,
)

internal fun categoryGroupChoices(
  groups: List<BudgetCategoryGroups>,
  categories: List<BudgetCategories>,
): ImmutableList<CategoryGroupChoice> {
  val byGroup = categories.groupBy { it.group_id }
  return groups
    .map { group ->
      CategoryGroupChoice(
        id = group.id,
        name = group.name.orEmpty(),
        isHidden = group.hidden,
        categories =
          byGroup[group.id]
            .orEmpty()
            .map { CategoryChoice(it.id, it.name.orEmpty(), it.hidden) }
            .toImmutableList(),
      )
    }
    .toImmutableList()
}

// packages/desktop-client/src/components/reports/reports/CustomReport.tsx
// isComplexCategoryCondition: the category selector can only stand in for one plain category
// condition
fun List<Condition>.hasComplexCategoryCondition(): Boolean {
  val categoryConditions = filter { it.field == Category }
  return categoryConditions.size > 1 ||
    categoryConditions.any { it.operator in COMPLEX_OPERATORS } ||
    any { it.field == CategoryGroup }
}

// CustomReport.tsx useSelectedCategories(): every category unless a category condition narrows it
fun List<Condition>.selectedCategories(all: List<CategoryId>): ImmutableSet<CategoryId> {
  val condition = firstOrNull { it.field == Category } ?: return all.toImmutableSet()
  val ids = condition.value.ids()
  val operator = condition.operator
  return when (operator) {
    Is,
    OneOf -> all.filter { it in ids }
    IsNot,
    NotOneOf -> all.filter { it !in ids }
    Contains,
    DoesNotContain,
    GreaterThan,
    GreaterThanOrEquals,
    HasAnyTag,
    HasTags,
    IsApprox,
    IsBetween,
    LessThan,
    LessThanOrEquals,
    Matches,
    OffBudget,
    OnBudget -> all
  }.toImmutableSet()
}

// CustomReport.tsx setSelectedCategories(): selecting everything removes the condition, a "not one
// of" condition stays that way, and anything else becomes "one of"
fun List<Condition>.withSelectedCategories(
  selected: Set<CategoryId>,
  all: List<CategoryId>,
): ImmutableList<Condition> {
  val index = indexOfFirst { it.field == Category }
  val allSelected = all.all { it in selected }
  if (index < 0) {
    return if (allSelected) toImmutableList() else (this + oneOf(selected, all)).toImmutableList()
  }

  val existing = get(index)
  val replacement =
    when {
      allSelected -> null
      existing.operator == NotOneOf ->
        existing.copy(value = idArray(all.filter { it !in selected }))
      else -> oneOf(selected, all)
    }
  return toMutableList()
    .apply { if (replacement == null) removeAt(index) else set(index, replacement) }
    .toImmutableList()
}

// Kept in the categories' order, so the same selection always makes the same condition
private fun oneOf(selected: Set<CategoryId>, all: List<CategoryId>) =
  Condition(
    field = Category,
    operator = OneOf,
    value = idArray(all.filter { it in selected }),
    type = Id,
  )

private fun idArray(ids: List<CategoryId>) = JsonArray(ids.map { JsonPrimitive(it.value) })

private fun JsonElement.ids(): Set<CategoryId> =
  when (this) {
      is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.takeIf { p -> p.isString } }
      is JsonPrimitive -> listOfNotNull(takeIf { isString })
      is JsonObject -> emptyList()
    }
    .map { CategoryId(it.content) }
    .toSet()

private val COMPLEX_OPERATORS =
  setOf<Operator>(
    Contains,
    DoesNotContain,
    Matches,
    HasTags,
    HasAnyTag,
  )
