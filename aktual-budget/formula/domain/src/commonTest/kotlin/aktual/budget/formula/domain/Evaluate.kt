package aktual.budget.formula.domain

import aktual.test.TestCalendar
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonthRange

// Serial number 45322
internal val TODAY = LocalDate(year = 2024, month = 1, day = 31)

internal suspend fun evaluate(
  formula: String,
  variables: Map<String, FormulaValue> = emptyMap(),
): FormulaValue = evaluateWithWarnings(formula, variables).value

internal suspend fun evaluateWithWarnings(
  formula: String,
  variables: Map<String, FormulaValue> = emptyMap(),
  data: FormulaDataSource = FakeFormulaDataSource(),
): FormulaResult = FormulaEvaluator(TestCalendar(TODAY), data).evaluate(formula, variables)

internal data class FakeQuery(
  val sum: Long = 0,
  val count: Long = 0,
  val categories: List<String> = emptyList(),
  val months: YearMonthRange? = null,
)

internal class FakeFormulaDataSource(
  private val queries: Map<String, FakeQuery> = emptyMap(),
  private val balances: Map<String, Long> = emptyMap(),
) : FormulaDataSource {
  override suspend fun querySum(name: String) = queries[name]?.sum

  override suspend fun queryCount(name: String) = queries[name]?.count

  override suspend fun queryCategories(name: String) = queries[name]?.categories

  override suspend fun queryMonths(name: String) = queries[name]?.months

  override suspend fun accountBalance(account: String) = balances[account]
}
