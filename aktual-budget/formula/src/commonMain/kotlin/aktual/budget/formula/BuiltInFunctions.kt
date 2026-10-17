package aktual.budget.formula

import aktual.core.Calendar

internal fun builtInFunctions(calendar: Calendar): Map<String, FormulaFunction> =
  LOGICAL_FUNCTIONS +
    MATH_FUNCTIONS +
    TEXT_FUNCTIONS +
    INFO_FUNCTIONS +
    FINANCIAL_FUNCTIONS +
    CONDITIONAL_FUNCTIONS +
    LOOKUP_FUNCTIONS +
    dateFunctions(calendar)
