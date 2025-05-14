package actual.budget.sync.vm

import kotlin.math.abs
import kotlin.math.roundToInt

@JvmInline
value class Percent(val value: Int) : Comparable<Percent> {
  constructor(value: Number) : this(value.toDouble().roundToInt())

  constructor(numerator: Number, denominator: Number) : this(safeFraction(numerator, denominator))

  override fun compareTo(other: Percent): Int = value.compareTo(other.value)

  override fun toString(): String = "$value%"

  @Suppress("MagicNumber")
  companion object {
    val OneHundred = 100.percent
    val Zero = 0.percent

    private fun safeFraction(numerator: Number, denominator: Number): Int {
      val n = numerator.toDouble()
      val d = denominator.toDouble()
      return if (n.isValid() && d.isValid()) (100.0 * n / d).roundToInt() else 0
    }

    private fun Double.isValid() = !isNaN() && isFinite() && abs(this) > 1e-5
  }
}

val Number.percent: Percent get() = Percent(value = this)
