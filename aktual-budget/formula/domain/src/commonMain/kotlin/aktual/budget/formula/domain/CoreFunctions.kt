package aktual.budget.formula.domain

internal val CORE_FUNCTIONS: Map<String, FormulaFunction> =
  mapOf(
    "FALSE" to formulaFunction { BooleanValue(false) },
    "TRUE" to formulaFunction { BooleanValue(true) },
  )
