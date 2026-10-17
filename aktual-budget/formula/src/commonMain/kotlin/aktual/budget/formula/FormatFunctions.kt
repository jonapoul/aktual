package aktual.budget.formula

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.truncate

// Port of the formatting functions in upstream's loot-core/src/shared/formulas/customFunctions.ts.
// Optional arguments are null when left out, which is when upstream falls back to the preferences
internal fun formatFunctions(format: FormulaFormat): Map<String, FormulaFunction> =
  mapOf(
    "INTEGER_TO_AMOUNT" to
      formulaFunction(Param(Numeric), Param(Numeric, default = NumberValue(2.0))) { args ->
        NumberValue(args.number(0) / 10.0.pow(args.number(1)))
      },
    "FIXED" to
      formulaFunction(Param(Numeric), Param(Numeric, default = NumberValue(0.0))) { args ->
        toFixed(args.number(0), args.number(1))?.let(::TextValue) ?: invalidDecimals()
      },
    "FORMATNUMBER" to
      formulaFunction(
        Param(Numeric),
        Param(Numeric, optional = true),
        Param(Text, optional = true),
        Param(Text, optional = true),
      ) { args ->
        val text =
          formatAmount(
            value = args.number(0),
            decimals = args.numberOrNull(1) ?: format.decimalPlaces.toDouble(),
            thousandsSeparator = args.textOrNull(2) ?: format.thousandsSeparator,
            decimalSeparator = args.textOrNull(3) ?: format.decimalSeparator,
          )
        text?.let(::TextValue) ?: invalidDecimals()
      },
    "FORMATCURRENCY" to
      formulaFunction(
        Param(Numeric),
        Param(Text, optional = true),
        Param(Numeric, optional = true),
        Param(Text, optional = true),
        Param(Text, optional = true),
        Param(Text, optional = true),
        Param(Logical, optional = true),
      ) { args ->
        formatCurrency(args, format)
      },
  )

private fun formatCurrency(args: FunctionArgs, format: FormulaFormat): FormulaValue {
  val value = args.number(0)
  val amount =
    formatAmount(
      value = abs(value),
      decimals = args.numberOrNull(2) ?: format.currencyDecimalPlaces.toDouble(),
      thousandsSeparator = args.textOrNull(3) ?: format.thousandsSeparator,
      decimalSeparator = args.textOrNull(4) ?: format.decimalSeparator,
    ) ?: return invalidDecimals()
  val symbol = args.textOrNull(1) ?: format.currencySymbol
  val symbolAfterAmount =
    when (args.textOrNull(5)) {
      "before" -> false
      "after" -> true
      else -> format.symbolAfterAmount
    }
  val hasSpace = args.booleanOrNull(6) ?: format.spaceBetweenAmountAndSymbol
  val space = if (hasSpace) NARROW_NO_BREAK_SPACE else ""
  val text = if (symbolAfterAmount) amount + space + symbol else symbol + space + amount
  return TextValue(if (value < 0) "-$text" else text)
}

private fun formatAmount(
  value: Double,
  decimals: Double,
  thousandsSeparator: String,
  decimalSeparator: String,
): String? {
  val parts = toFixed(value, decimals)?.split('.') ?: return null
  val integer = parts[0]
  // Upstream uses String.replace, so the separator can use its replacement patterns
  val grouped =
    THOUSANDS.replace(integer) { match ->
      substitution(
        text = integer,
        index = match.range.first,
        match = match.value,
        replacement = thousandsSeparator,
        groups = match.groupValues.drop(1),
      )
    }
  val fraction = parts.getOrNull(1)
  val hasFraction = decimals > 0 && !fraction.isNullOrEmpty()
  return if (hasFraction) grouped + decimalSeparator + fraction else grouped
}

// Number.toFixed truncates the digits, and throws outside 0 to 100
private fun toFixed(value: Double, digits: Double): String? {
  val wholeDigits = truncate(digits)
  if (wholeDigits !in 0.0..MAX_DIGITS) return null
  return jsToFixed(value, wholeDigits.toInt())
}

private fun invalidDecimals() = ErrorValue(Value, DecimalsOutOfRange)

private fun FunctionArgs.numberOrNull(index: Int): Double? = (this[index] as? NumberValue)?.value

private fun FunctionArgs.textOrNull(index: Int): String? = (this[index] as? TextValue)?.value

private fun FunctionArgs.booleanOrNull(index: Int): Boolean? = (this[index] as? BooleanValue)?.value

private val THOUSANDS = Regex("""\B(?=(\d{3})+(?!\d))""")
private const val MAX_DIGITS = 100.0
