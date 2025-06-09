package actual.budget.model

import kotlin.math.roundToLong

/**
 * Stores an amount of money, e.g. £123.45 is stored in [value] as the integer 12345.
 */
@JvmInline
value class Amount(private val value: Long) : Comparable<Amount> {
  constructor(value: Int) : this(value.toLong())
  constructor(value: Short) : this(value.toLong())
  constructor(value: Float) : this((value * FACTOR).roundToLong())
  constructor(value: Double) : this((value * FACTOR).roundToLong())

  override fun toString(): String = toString(Currency.default())

  override fun compareTo(other: Amount): Int = value.compareTo(other.value)

  fun toString(currency: Currency): String = "%c%.2f".format(currency.symbol, toFloat())

  fun toFloat(): Float = (value / FACTOR).toFloat()

  fun toLong(): Long = value

  companion object {
    private const val FACTOR = 100.0
  }
}
