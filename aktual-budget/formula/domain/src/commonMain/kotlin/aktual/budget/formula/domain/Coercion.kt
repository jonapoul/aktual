package aktual.budget.formula.domain

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

// Ports of HyperFormula's ArithmeticHelper coercions, using its default config: smart rounding on,
// '.' decimals, no thousands separator, '$' currency and case/accent insensitive text comparison

internal const val PRECISION_EPSILON = 1e-13
private const val PRECISION_ROUNDING = 10

internal const val NUMBER_COERCION_MESSAGE = "Value cannot be coerced to number."

internal fun FormulaValue.toScalar(): FormulaValue = if (this is ArrayValue) topLeft else this

internal fun coerceToNumber(value: FormulaValue): FormulaValue =
  when (val scalar = value.toScalar()) {
    is ErrorValue,
    is NumberValue -> scalar
    is BooleanValue -> NumberValue(if (scalar.value) 1.0 else 0.0)
    EmptyValue -> NumberValue(0.0)
    is TextValue ->
      textToNumber(scalar.value)?.let(::NumberValue)
        ?: ErrorValue(FormulaErrorType.Value, NUMBER_COERCION_MESSAGE)
    is ArrayValue -> error("Unreachable")
  }

internal fun textToNumber(text: String): Double? {
  if (text.isEmpty()) return 0.0
  val trimmed = text.trim()
  if (trimmed.endsWith('%')) {
    parseNumericString(trimmed.dropLast(1).trim())?.let {
      return it / 100
    }
  }
  currencyAmount(trimmed)?.let {
    return it
  }
  return parseNumericString(trimmed) ?: dateStringToNumber(text)
}

private fun currencyAmount(text: String): Double? {
  val amount =
    when {
      text.startsWith(CURRENCY_SYMBOL) -> text.removePrefix(CURRENCY_SYMBOL).trim()
      text.endsWith(CURRENCY_SYMBOL) -> text.removeSuffix(CURRENCY_SYMBOL).trim()
      else -> return null
    }
  return parseNumericString(amount)
}

private fun parseNumericString(text: String): Double? =
  if (NUMERIC_STRING.matches(text)) text.toDoubleOrNull() else null

internal fun coerceToText(value: FormulaValue): FormulaValue =
  when (val scalar = value.toScalar()) {
    is ErrorValue,
    is TextValue -> scalar
    is NumberValue -> TextValue(jsNumberToString(scalar.value))
    is BooleanValue -> TextValue(if (scalar.value) "TRUE" else "FALSE")
    EmptyValue -> TextValue("")
    is ArrayValue -> error("Unreachable")
  }

// Null when the value can't be read as a boolean, which callers report as a wrong type
internal fun coerceToBoolean(value: FormulaValue): FormulaValue? =
  when (val scalar = value.toScalar()) {
    is ErrorValue,
    is BooleanValue -> scalar
    is NumberValue -> BooleanValue(scalar.value != 0.0)
    EmptyValue -> BooleanValue(false)
    is TextValue ->
      when (scalar.value.uppercase()) {
        "TRUE" -> BooleanValue(true)
        "FALSE",
        "" -> BooleanValue(false)
        else -> null
      }
    is ArrayValue -> error("Unreachable")
  }

// Neither side may be an error or array
internal fun compareValues(left: FormulaValue, right: FormulaValue): Int {
  if (left is TextValue || right is TextValue) {
    val leftDate = if (left is TextValue) dateStringToNumber(left.value) else left.numberOrNull()
    val rightDate =
      if (right is TextValue) dateStringToNumber(right.value) else right.numberOrNull()
    if (leftDate != null && rightDate != null) return floatCompare(leftDate, rightDate)
  }

  var l = left
  var r = right
  if (l == EmptyValue) {
    l = r.emptyEquivalent()
  } else if (r == EmptyValue) {
    r = l.emptyEquivalent()
  }

  return when {
    l is TextValue && r is TextValue -> compareText(l.value, r.value)
    l is BooleanValue && r is BooleanValue -> l.value.compareTo(r.value)
    l is NumberValue && r is NumberValue -> floatCompare(l.value, r.value)
    l == EmptyValue && r == EmptyValue -> 0
    else -> l.typeOrder().compareTo(r.typeOrder())
  }
}

private fun FormulaValue.numberOrNull(): Double? = (this as? NumberValue)?.value

private fun FormulaValue.emptyEquivalent(): FormulaValue =
  when (this) {
    is TextValue -> TextValue("")
    is NumberValue -> NumberValue(0.0)
    is BooleanValue -> BooleanValue(false)
    else -> EmptyValue
  }

private fun FormulaValue.typeOrder(): Int =
  when (this) {
    EmptyValue -> 0
    is NumberValue -> 1
    is TextValue -> 2
    is BooleanValue -> 3
    is ErrorValue,
    is ArrayValue -> 4
  }

internal expect fun compareText(left: String, right: String): Int

internal fun floatCompare(left: Double, right: Double): Int {
  val mod = 1 + PRECISION_EPSILON
  return when {
    right >= 0 && left * mod >= right && left <= right * mod -> 0
    right <= 0 && left * mod <= right && left >= right * mod -> 0
    left > right -> 1
    else -> -1
  }
}

internal fun addWithEpsilon(left: Double, right: Double): Double {
  val result = left + right
  return if (abs(result) < PRECISION_EPSILON * abs(left)) 0.0 else result
}

internal fun subtractWithEpsilon(left: Double, right: Double): Double {
  val result = left - right
  return if (abs(result) < PRECISION_EPSILON * abs(left)) 0.0 else result
}

// HyperFormula rounds numbers to 10 significant digits when reading a cell's value
internal fun roundForOutput(value: Double): Double {
  if (value == 0.0) return value
  val magnitude = floor(log10(abs(value)))
  val multiplier = 10.0.pow(PRECISION_ROUNDING - magnitude)
  return if (value < 0) -jsRound(-value * multiplier) / multiplier
  else jsRound(value * multiplier) / multiplier
}

// JavaScript's Math.round, which rounds halves up rather than to even
internal fun jsRound(value: Double): Double = floor(value + 0.5)

// Matches JavaScript's Number.prototype.toString, which is how HyperFormula turns numbers into text
internal fun jsNumberToString(value: Double): String {
  if (value.isNaN()) return "NaN"
  if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
  if (value == 0.0) return "0"
  if (value < 0) return "-" + jsNumberToString(-value)

  val javaString = value.toString()
  val mantissa = javaString.substringBefore('E')
  val exponent = javaString.substringAfter('E', missingDelimiterValue = "0").toInt()
  val integerPart = mantissa.substringBefore('.')
  val allDigits = integerPart + mantissa.substringAfter('.', missingDelimiterValue = "")
  val leadingZeros = allDigits.length - allDigits.trimStart('0').length
  val digits = allDigits.trimStart('0').trimEnd('0')
  val k = digits.length
  val n = integerPart.length + exponent - leadingZeros

  return when {
    n in k..MAX_PLAIN_EXPONENT -> digits + "0".repeat(n - k)
    n in 1..MAX_PLAIN_EXPONENT -> digits.substring(0, n) + "." + digits.substring(n)
    n in MIN_PLAIN_EXPONENT..0 -> "0." + "0".repeat(-n) + digits
    else -> {
      val e = n - 1
      val sign = if (e >= 0) "+" else "-"
      val significand = if (k == 1) digits else digits.first() + "." + digits.substring(1)
      "${significand}e$sign${abs(e)}"
    }
  }
}

private const val MAX_PLAIN_EXPONENT = 21
private const val MIN_PLAIN_EXPONENT = -5
private const val CURRENCY_SYMBOL = "$"
private val NUMERIC_STRING = Regex("[+-]?((\\.\\d+)|(\\d+(\\.\\d*)?))(e[+-]?\\d+)?")
