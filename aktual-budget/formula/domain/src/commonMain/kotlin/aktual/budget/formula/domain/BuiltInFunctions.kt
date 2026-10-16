package aktual.budget.formula.domain

import aktual.core.Calendar

internal fun builtInFunctions(calendar: Calendar): Map<String, FormulaFunction> =
  LOGICAL_FUNCTIONS + MATH_FUNCTIONS + TEXT_FUNCTIONS + INFO_FUNCTIONS + dateFunctions(calendar)
