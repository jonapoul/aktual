package aktual.budget.reports.vm

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOptions
import aktual.budget.model.ConditionSet
import aktual.budget.model.ConditionsFilter
import aktual.budget.model.ExactTags
import aktual.budget.model.Field
import aktual.budget.model.Operator
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/**
 * The part of a custom report a tap drills down to: a [group] in an [interval]. A null [group]
 * covers every group, and a null [interval] the report's whole dates. [flow] narrows it to money in
 * or out, as the table's deposits and payments columns do
 */
@Immutable
data class CustomDrilldown(
  val group: CustomGroup? = null,
  val interval: CustomInterval? = null,
  val flow: CustomFlow? = null,
)

enum class CustomFlow {
  In,
  Out,
}

// packages/desktop-client/src/components/reports/graphs/showActivity.ts: the report's own
// conditions, plus the group, dates, flow and shown rows the tapped amount was summed from. Unlike
// upstream, the made-up groups (uncategorised, transfers, off budget) filter down to their own
// transactions. Null for budgeted reports, whose amounts aren't transactions
fun CustomData.drilldownFilter(drilldown: CustomDrilldown): ConditionsFilter? {
  if (!canDrillDown) return null
  val group = drilldown.group
  val sets = buildList {
    if (config.conditions.isNotEmpty()) add(ConditionSet(config.conditions, config.conditionsOp))
    add(
      ConditionSet(dateConditions(drilldownDates(drilldown.interval)) + flowConditions(drilldown)),
    )
    group?.let { addAll(it.conditionSets()) }
    addAll(shownRows())
  }
  val tags =
    when (group) {
      is TagSet -> ExactTags(group.names, tagScope)
      Untagged -> ExactTags(emptyList(), tagScope)
      is Category,
      is Group,
      is Payee,
      is Account,
      Uncategorized,
      Transfers,
      OffBudget,
      UncategorizedAndOffBudget,
      null -> null
    }
  return ConditionsFilter(sets.filter { it.conditions.isNotEmpty() }, tags)
}

// Monthly and yearly reports sum the whole of their first and last month or year, see
// customReportQuery()
private fun CustomData.drilldownDates(interval: CustomInterval?): LocalDateRange {
  if (interval == null) {
    val query = customReportQuery(config.interval, range)
    return query.start..query.end
  }
  val start = interval.start
  return when (config.interval) {
    Monthly -> start.yearMonth.firstDay..start.yearMonth.lastDay
    Yearly ->
      LocalDate(start.year, JANUARY, 1)..LocalDate(start.year, DECEMBER, LAST_DAY_OF_DECEMBER)
    Daily,
    Weekly,
    Unknown -> interval.range
  }
}

private fun dateConditions(dates: LocalDateRange) =
  listOf(
    condition(Date, GreaterThanOrEquals, JsonPrimitive(dates.start.toString())),
    condition(Date, LessThanOrEquals, JsonPrimitive(dates.endInclusive.toString())),
  )

// Reports of only money in or out sum only that, net and total reports sum everything
private fun CustomData.flowConditions(drilldown: CustomDrilldown): List<Condition> {
  val flow: CustomFlow? =
    drilldown.flow
      ?: when (config.balanceType.metric) {
        Assets -> In
        Debts -> Out
        NetAssets,
        NetDebts,
        Total -> null
      }
  val options =
    when (flow) {
      In -> ConditionOptions(inflow = true)
      Out -> ConditionOptions(outflow = true)
      null -> return emptyList()
    }
  return listOf(condition(Amount, GreaterThanOrEquals, JsonPrimitive(0), options))
}

// How customSpreadsheet() buckets each row: categorised on-budget rows go to their category and
// group, the rest to one of the made-up ones
private fun CustomGroup.conditionSets(): List<ConditionSet> =
  when (this) {
    is Category -> and(isId(Category, id.value), onBudget())
    is Group -> and(isId(CategoryGroup, id.value), onBudget())
    is Payee -> and(isId(Payee, id.value))
    is Account -> and(isId(Account, id.value))
    Uncategorized -> and(noCategory(), isTransfer(false), onBudget())
    Transfers -> and(noCategory(), isTransfer(true), onBudget())
    OffBudget -> and(offBudget())
    UncategorizedAndOffBudget -> listOf(ConditionSet(listOf(noCategory(), offBudget()), Or))
    is TagSet,
    Untagged -> emptyList()
  }

// filterReportTransactions(), as customSpreadsheet() leaves out rows the report doesn't show
private fun CustomData.shownRows(): List<ConditionSet> = buildList {
  if (!config.showHidden && hiddenCategories.isNotEmpty()) {
    val ids = JsonArray(hiddenCategories.map { JsonPrimitive(it.value) })
    add(ConditionSet(listOf(condition(Category, NotOneOf, ids))))
  }
  if (!config.showOffBudget) add(ConditionSet(listOf(onBudget())))
  if (!config.showUncategorized) {
    val anyCategory = condition(Category, NotOneOf, JsonArray(listOf(JsonNull)))
    add(ConditionSet(listOf(anyCategory, offBudget()), Or))
  }
}

private fun and(vararg conditions: Condition) = listOf(ConditionSet(conditions.toList()))

private fun isId(field: Field, id: String) = condition(field, Is, JsonPrimitive(id))

// "is nothing" leaves out transfers, so this matches a missing category with oneOf instead
private fun noCategory() = condition(Category, OneOf, JsonArray(listOf(JsonNull)))

private fun isTransfer(value: Boolean) = condition(Transfer, Is, JsonPrimitive(value))

private fun onBudget() = condition(Account, OnBudget, JsonNull)

private fun offBudget() = condition(Account, OffBudget, JsonNull)

private fun condition(
  field: Field,
  operator: Operator,
  value: JsonElement,
  options: ConditionOptions? = null,
) = Condition(field = field, operator = operator, value = value, options = options)

/** Budgeted reports have no transactions to show */
val CustomData.canDrillDown: Boolean
  get() = config.balanceType != Budgeted
