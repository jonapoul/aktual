package aktual.budget.reports.vm

import aktual.budget.formula.domain.FormulaValue
import aktual.budget.formula.domain.TextValue
import aktual.core.theme.LightColors
import aktual.core.theme.attributes

// The colour formula's theme_* variables. The VM doesn't know the current theme, so each one is its
// own name, which the UI swaps for the theme's colour
const val FORMULA_THEME_PREFIX = "theme_"

internal val FORMULA_THEME_VARIABLES: Map<String, FormulaValue> =
  LightColors.attributes().keys.associate { name ->
    "$FORMULA_THEME_PREFIX$name" to TextValue("$FORMULA_THEME_PREFIX$name")
  }
