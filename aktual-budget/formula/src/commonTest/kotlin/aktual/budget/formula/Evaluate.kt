package aktual.budget.formula

import aktual.test.TestCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

// Serial number 45322
internal val TODAY = LocalDate(year = 2024, month = 1, day = 31)

// With TODAY, serial number 45322.52100694...
internal val NOW = LocalTime(hour = 12, minute = 30, second = 15)

internal suspend fun evaluate(
  formula: String,
  variables: Map<String, FormulaValue> = emptyMap(),
): FormulaValue = evaluateWithWarnings(formula, variables).value

internal suspend fun evaluateWithWarnings(
  formula: String,
  variables: Map<String, FormulaValue> = emptyMap(),
  data: FormulaDataSource = FakeFormulaDataSource(),
  format: FormulaFormat = FormulaFormat(),
): FormulaResult =
  FormulaEvaluator(TestCalendar(TODAY, NOW), data, format).evaluate(formula, variables)

internal data class FakeQuery(
  val sum: Long = 0,
  val count: Long = 0,
  val categories: List<String> = emptyList(),
  val months: YearMonthRange? = null,
)

internal class FakeFormulaDataSource(
  private val queries: Map<String, FakeQuery> = emptyMap(),
  private val balances: Map<String, Long> = emptyMap(),
  private val budgets: Map<YearMonth, Map<String, CategoryBudget>> = emptyMap(),
) : FormulaDataSource {
  override suspend fun querySum(name: String) = queries[name]?.sum

  override suspend fun queryCount(name: String) = queries[name]?.count

  override suspend fun queryCategories(name: String) = queries[name]?.categories

  override suspend fun queryMonths(name: String) = queries[name]?.months

  override suspend fun accountBalance(account: String) = balances[account]

  override suspend fun budgetMonth(month: YearMonth) = budgets[month].orEmpty()
}
