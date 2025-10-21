/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.core.model

import kotlin.math.abs
import kotlin.math.roundToInt

@JvmInline
value class Percent(private val value: Double) : Comparable<Percent> {
  val intValue: Int get() = value.roundToInt()
  val floatValue: Float get() = value.toFloat()
  val doubleValue: Double get() = value

  constructor(value: Number) : this(value.toDouble())

  constructor(numerator: Number, denominator: Number) : this(safeFraction(numerator, denominator))

  override fun compareTo(other: Percent): Int = value.compareTo(other.value)

  override fun toString(): String = toString(decimalPlaces = 0)

  fun toString(decimalPlaces: Int): String = "%.${decimalPlaces}f%%".format(value)

  @Suppress("MagicNumber")
  companion object {
    val OneHundred = 100.percent
    val Zero = 0.percent

    private fun safeFraction(numerator: Number, denominator: Number): Double {
      val n = numerator.toDouble()
      val d = denominator.toDouble()
      return if (n.isValid() && d.isValid()) 100.0 * n / d else 0.0
    }

    private fun Double.isValid() = !isNaN() && isFinite() && abs(this) > 1e-5
  }
}

val Number.percent: Percent get() = Percent(value = this)
