package aktual.budget.formula.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

internal actual fun jsToFixed(value: Double, digits: Int): String =
  if (abs(value) >= MAX_FIXED) {
    jsNumberToString(value)
  } else {
    // Needs the exact binary value, which BigDecimal.valueOf would round first
    @Suppress("ForbiddenMethodCall")
    BigDecimal(value).setScale(digits, RoundingMode.HALF_UP).toPlainString()
  }

private const val MAX_FIXED = 1e21
