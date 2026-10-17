package aktual.budget.formula.domain

import java.math.BigDecimal
import java.math.RoundingMode

internal actual fun jsToFixed(value: Double, digits: Int): String =
  when {
    // Keeps the sign when rounding to zero, e.g. "-0.00"
    value < 0 -> "-" + jsToFixed(-value, digits)
    value >= MAX_FIXED -> jsNumberToString(value)
    // Needs the exact binary value, which BigDecimal.valueOf would round first
    else ->
      @Suppress("ForbiddenMethodCall")
      BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toPlainString()
  }

private const val MAX_FIXED = 1e21
