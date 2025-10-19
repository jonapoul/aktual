/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
