package aktual.budget.model

import kotlin.math.abs

/**
 * Reads a typed, unsigned amount like "1,200.50" or "1.200,50". The last separator counts as the
 * decimal point when one or two digits follow it, otherwise separators are ignored. Blank input is
 * zero.
 */
fun parseAmountInput(text: String): Amount? {
  if (text.isBlank()) return Zero
  val cleaned = text.filter { it.isDigit() || it == '.' || it == ',' }
  if (cleaned.none { it.isDigit() }) return null

  val separator = cleaned.indexOfLast { it == '.' || it == ',' }
  val decimals = cleaned.length - separator - 1
  val hasFraction = separator >= 0 && decimals in 0..MAX_DECIMALS
  val whole = if (hasFraction) cleaned.substring(0, separator) else cleaned
  val fraction = if (hasFraction) cleaned.substring(separator + 1) else ""

  val digits = whole.filter { it.isDigit() }.ifEmpty { "0" } + fraction.padEnd(MAX_DECIMALS, '0')
  return digits.toLongOrNull()?.let { Amount(it) }
}

/** The unsigned amount as plain text to edit, like "1200.50", or blank for zero */
fun Amount.toInputText(): String {
  val cents = abs(toLong())
  if (cents == 0L) return ""
  return "${cents / CENTS}.${(cents % CENTS).toString().padStart(MAX_DECIMALS, '0')}"
}

private const val MAX_DECIMALS = 2
private const val CENTS = 100
