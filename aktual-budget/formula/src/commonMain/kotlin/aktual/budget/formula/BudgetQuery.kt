package aktual.budget.formula

import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// Port of BUDGET_QUERY in upstream's loot-core/src/shared/formulas/customFunctions.ts and
// fetchBudgetDimensionValueDirect() in desktop-client/src/hooks/useFormulaExecution.ts
internal fun budgetQueryFunction(data: FormulaDataSource): FormulaFunction =
  formulaFunction(Param(Text), Param(Range), Param(Text), Param(Text)) { args ->
    val dimension = args.text(0).lowercase()
    val start = args.text(2).toYearMonthOrNull()
    val end = args.text(3).toYearMonthOrNull()
    when {
      dimension !in DIMENSIONS -> {
        ErrorValue(Value, InvalidBudgetDimension, args.text(0))
      }
      start == null || end == null -> {
        ErrorValue(Value, InvalidDate)
      }
      end.monthIndex() - start.monthIndex() >= MAX_MONTHS -> {
        ErrorValue(Num, DateOutOfRange)
      }
      else -> {
        val months = generateSequence(start) { it.plus(1, MONTH) }.takeWhile { it <= end }.toList()
        val categories = categoryIds(args.value(1))
        NumberValue(budgetValue(data, dimension, categories, months) / CENTS)
      }
    }
  }

private val DIMENSIONS = setOf("budgeted", "spent", "goal", "balance_start", "balance_end")

private val YEAR_MONTH = Regex("""(\d{4})-(\d{2})""")

private const val CENTS = 100.0
private const val MONTHS_IN_YEAR = 12

// Every month costs a database read, so a typo can't ask for thousands of years
private const val MAX_MONTHS = 1200

private fun YearMonth.monthIndex(): Int = year * MONTHS_IN_YEAR + month.ordinal

private val NO_BUDGET =
  CategoryBudget(budgeted = 0, spent = 0, goal = 0, balance = 0, carryover = false)

// Anything after the month, like a day, is ignored
private fun String.toYearMonthOrNull(): YearMonth? {
  val (year, month) = YEAR_MONTH.matchAt(this, 0)?.destructured ?: return null
  return if (month.toInt() in 1..MONTHS_IN_YEAR) YearMonth(year.toInt(), month.toInt()) else null
}

// Only text and numbers count, and each can hold several ids separated by commas
private fun categoryIds(range: FormulaValue): List<String> =
  (range as ArrayValue)
    .rows
    .asSequence()
    .flatten()
    .mapNotNull {
      if (it is TextValue || it is NumberValue) coerceToText(it) as? TextValue else null
    }
    .flatMap { it.value.split(',') }
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .toList()

private suspend fun budgetValue(
  data: FormulaDataSource,
  dimension: String,
  categories: List<String>,
  months: List<YearMonth>,
): Long {
  suspend fun rows(month: YearMonth): List<CategoryBudget> {
    val budgets = data.budgetMonth(month)
    return categories.map { budgets[it] ?: NO_BUDGET }
  }

  return when (dimension) {
    "budgeted" -> {
      months.sumOf { month -> rows(month).sumOf { it.budgeted } }
    }
    "spent" -> {
      months.sumOf { month -> rows(month).sumOf { it.spent } }
    }
    "goal" -> {
      months.sumOf { month -> rows(month).sumOf { it.goal } }
    }
    else -> {
      val first = months.firstOrNull() ?: return 0L
      // Balances chain from the month before the first, one month at a time
      var carried = rows(first.minus(1, MONTH)).sumOf { it.carried() }
      val starting = carried
      var ending = 0L
      for (month in months) {
        val rows = rows(month)
        ending = rows.sumOf { it.budgeted } + rows.sumOf { it.spent } + carried
        carried = rows.sumOf { it.carried() }
      }
      if (dimension == "balance_start") starting else ending
    }
  }
}

// Overspending only carries over if the category is set to
private fun CategoryBudget.carried(): Long =
  if (balance > 0 || balance < 0 && carryover) balance else 0L
