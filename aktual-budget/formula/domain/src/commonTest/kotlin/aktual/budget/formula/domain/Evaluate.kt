package aktual.budget.formula.domain

import aktual.test.TestCalendar
import kotlinx.datetime.LocalDate

// Serial number 45322
internal val TODAY = LocalDate(year = 2024, month = 1, day = 31)

internal suspend fun evaluate(
  formula: String,
  variables: Map<String, FormulaValue> = emptyMap(),
): FormulaValue = FormulaEvaluator(TestCalendar(TODAY)).evaluate(formula, variables).value
