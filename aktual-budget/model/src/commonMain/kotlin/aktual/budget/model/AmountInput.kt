package aktual.budget.model

import kotlin.math.abs
import kotlin.math.round

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
private const val CENTS_PER_UNIT = 100.0

/**
 * Reads typed arithmetic like "120 + 35.50" or "-1,200 ÷ 3", as upstream's amount inputs do with
 * evalArithmetic. Operands are read by [parseAmountInput], × and ÷ bind tighter than + and −, and
 * the result is rounded to the cent. Blank input is zero.
 */
fun evaluateAmountInput(text: String): Amount? {
  if (text.isBlank()) return Zero
  val tokens = tokenise(text) ?: return null
  return ArithmeticParser(tokens).parse()?.let { Amount(round(it * CENTS_PER_UNIT).toLong()) }
}

/** Like [toInputText], with a leading minus when negative */
fun Amount.toSignedInputText(): String = if (this < Zero) "-${toInputText()}" else toInputText()

private sealed interface Token {
  data class Number(val value: Double) : Token

  data class Operator(val symbol: Char) : Token
}

private fun tokenise(text: String): List<Token>? {
  val tokens = mutableListOf<Token>()
  val number = StringBuilder()
  fun flush(): Boolean {
    if (number.isEmpty()) return true
    val amount = parseAmountInput(number.toString()) ?: return false
    tokens += Token.Number(amount.toLong().toDouble() / CENTS_PER_UNIT)
    number.clear()
    return true
  }

  for (char in text) {
    val operator = OPERATORS[char]
    when {
      char.isDigit() || char == '.' || char == ',' -> number.append(char)
      operator != null -> if (flush()) tokens += Token.Operator(operator) else return null
      char.isWhitespace() -> continue
      else -> return null
    }
  }
  return if (flush()) tokens else null
}

private class ArithmeticParser(private val tokens: List<Token>) {
  private var position = 0

  fun parse(): Double? {
    val result = sum() ?: return null
    return result.takeIf { position == tokens.size && it.isFinite() }
  }

  private fun sum(): Double? {
    var result = product() ?: return null
    while (true) {
      val operator = (tokens.getOrNull(position) as? Token.Operator)?.symbol
      if (operator != '+' && operator != '-') return result
      position++
      val next = product() ?: return null
      result = if (operator == '+') result + next else result - next
    }
  }

  private fun product(): Double? {
    var result = unary() ?: return null
    while (true) {
      val operator = (tokens.getOrNull(position) as? Token.Operator)?.symbol
      if (operator != '*' && operator != '/') return result
      position++
      val next = unary() ?: return null
      result = if (operator == '*') result * next else result / next
    }
  }

  private fun unary(): Double? =
    when (val token = tokens.getOrNull(position++)) {
      is Token.Number -> token.value
      is Token.Operator if token.symbol == '-' -> unary()?.let { -it }
      is Token.Operator if token.symbol == '+' -> unary()
      else -> null
    }
}

// Typed and keypad symbols, mapped to what the parser reads
private val OPERATORS =
  mapOf(
    '+' to '+',
    '-' to '-',
    '−' to '-',
    '*' to '*',
    '×' to '*',
    '/' to '/',
    '÷' to '/',
  )
