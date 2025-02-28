package actual.budget.model

import kotlin.math.roundToLong

/**
 * Stores an amount of money, e.g. £123.45 is stored in [longAmount] as the integer 12345.
 */
@JvmInline
value class Amount(private val longAmount: Long) {
  override fun toString(): String = toString(Currency.default())

  fun toString(currency: Currency): String = "%c%.2f".format(currency.symbol, toFloat())

  fun toFloat(): Float = longAmount / FACTOR

  fun toLong(): Long = longAmount

  companion object {
    fun from(amount: Float): Amount = from(amount.toDouble())

    fun from(amount: Double): Amount {
      val long = (amount / FACTOR).roundToLong()
      return Amount(long)
    }

    private const val FACTOR = 100f
  }
}
