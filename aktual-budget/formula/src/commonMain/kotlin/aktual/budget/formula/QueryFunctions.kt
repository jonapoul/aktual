package aktual.budget.formula

import aktual.core.Calendar
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.yearMonth

// Port of the query functions in upstream's loot-core/src/shared/formulas/customFunctions.ts, with
// the fallbacks from desktop-client/src/hooks/useFormulaExecution.ts for unknown names
internal fun queryFunctions(
  data: FormulaDataSource,
  calendar: Calendar,
): Map<String, FormulaFunction> =
  mapOf(
    "QUERY" to
      queryFunction { name ->
        NumberValue((data.querySum(name) ?: unknownQuery(name, 0L)) / CENTS)
      },
    "QUERY_COUNT" to
      queryFunction { name ->
        NumberValue((data.queryCount(name) ?: unknownQuery(name, 0L)).toDouble())
      },
    "QUERY_EXTRACT_CATEGORIES" to
      queryFunction { name ->
        val ids = data.queryCategories(name) ?: unknownQuery(name, emptyList())
        ArrayValue(ids.ifEmpty { listOf("") }.map { listOf(TextValue(it)) })
      },
    "QUERY_EXTRACT_TIMEFRAME_START" to
      queryFunction { name -> TextValue(months(data, calendar, name).start.toString()) },
    "QUERY_EXTRACT_TIMEFRAME_END" to
      queryFunction { name -> TextValue(months(data, calendar, name).endInclusive.toString()) },
    "BALANCE_OF" to
      queryFunction { account ->
        val balance = data.accountBalance(account)
        if (balance == null) warn(UnknownAccount(account))
        NumberValue((balance ?: 0L) / CENTS)
      },
    "BUDGET_QUERY" to budgetQueryFunction(data),
  )

private fun queryFunction(body: suspend EvaluationScope.(String) -> FormulaValue): FormulaFunction =
  formulaFunction(Param(Text)) { args -> body(args.text(0)) }

private fun <T> EvaluationScope.unknownQuery(name: String, fallback: T): T {
  warn(UnknownQuery(name))
  return fallback
}

private suspend fun EvaluationScope.months(
  data: FormulaDataSource,
  calendar: Calendar,
  name: String,
): YearMonthRange {
  val months = data.queryMonths(name)
  if (months != null) return months
  val current = calendar.today().yearMonth
  return unknownQuery(name, current..current)
}

private const val CENTS = 100.0
