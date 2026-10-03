package aktual.budget.reports.vm

import kotlin.math.abs
import kotlin.math.roundToLong

// MonteCarloNumberInput.tsx: the text shown for a value, and how typed text is read back. The scale
// is 100 for percentage fields (0.06 shown as 6) and 1 for plain numbers

sealed interface NumberInput {
  // Not a number, so the field reverts to its last value
  data object Invalid : NumberInput

  // null when an optional field was cleared
  data class Valid(val value: Double?) : NumberInput
}

fun numberInputText(value: Double?, scale: Int = 1): String {
  if (value == null) return ""
  val scaled = (value * scale * DISPLAY_FACTOR).roundToLong()
  val whole = abs(scaled) / DISPLAY_FACTOR
  val fraction = (abs(scaled) % DISPLAY_FACTOR).toString().padStart(DISPLAY_PLACES, '0')
  val trimmed = fraction.trimEnd('0')
  val sign = if (scaled < 0) "-" else ""
  return if (trimmed.isEmpty()) "$sign$whole" else "$sign$whole.$trimmed"
}

fun parseNumberInput(
  text: String,
  min: Double,
  max: Double,
  scale: Int = 1,
  allowEmpty: Boolean = false,
  roundToInteger: Boolean = false,
): NumberInput {
  val trimmed = text.trim()
  if (trimmed.isEmpty()) return if (allowEmpty) NumberInput.Valid(null) else Invalid
  if (!NUMBER.matches(trimmed)) return Invalid
  val parsed = trimmed.replace(',', '.').toDoubleOrNull() ?: return Invalid
  val rounded = if (roundToInteger) parsed.roundToLong().toDouble() else parsed
  // Not coerceIn(), which throws when a stale plan has its bounds the wrong way round
  val clamped = minOf(max, maxOf(min, rounded))
  return NumberInput.Valid(clamped / scale)
}

private val NUMBER = Regex("""[+-]?(\d+([.,]\d*)?|[.,]\d+)""")
private const val DISPLAY_PLACES = 4
private const val DISPLAY_FACTOR = 10_000L
