package actual.budget.sync.vm

import kotlin.math.roundToInt

@JvmInline
value class Percent(val value: Int) : Comparable<Percent> {
  constructor(value: Number) : this(value.toDouble().roundToInt())

  constructor(numerator: Number, denominator: Number) : this(
    value = 100.0 * numerator.toDouble() / denominator.toDouble(),
  )

  override fun compareTo(other: Percent): Int = value.compareTo(other.value)

  override fun toString(): String = "$value%"

  companion object {
    val OneHundred = 100.percent
    val Zero = 0.percent
  }
}

val Number.percent: Percent get() = Percent(value = this)
