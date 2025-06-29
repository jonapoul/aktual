package actual.budget.model

import java.util.Locale
import kotlin.math.roundToLong
import java.text.NumberFormat as JNumberFormat

/**
 * Stores an amount of money, e.g. £123.45 is stored in [value] as the integer 12345.
 */
@JvmInline
value class Amount(private val value: Long) : Comparable<Amount> {
  constructor(value: Int) : this(value.toLong())
  constructor(value: Short) : this(value.toLong())
  constructor(value: Float) : this((value * FACTOR).roundToLong())
  constructor(value: Double) : this((value * FACTOR).roundToLong())

  override fun toString(): String = "%.2f".format(toFloat())

  override fun compareTo(other: Amount): Int = value.compareTo(other.value)

  fun toFloat(): Float = toDouble().toFloat()

  fun toDouble(): Double = value / FACTOR

  fun toLong(): Long = value

  fun isPositive() = this >= Zero

  @Suppress("MagicNumber")
  fun toString(
    config: NumberFormatConfig,
    includeSign: Boolean,
  ): String = buildString {
    if (includeSign) append(if (value > 0) "+" else "-")

    val (format, hideFraction) = config

    val locale = when (format) {
      NumberFormat.CommaDot -> Locale.US
      NumberFormat.DotComma -> Locale.GERMANY
      NumberFormat.SpaceComma -> enSe
      NumberFormat.ApostropheDot -> deCh
      NumberFormat.CommaDotIn -> enIn
    }

    val numberFormat = JNumberFormat.getNumberInstance(locale).apply {
      minimumFractionDigits = if (hideFraction) 0 else 2
      maximumFractionDigits = if (hideFraction) 0 else 2
    }

    append(numberFormat.format(toDouble()))
  }

  companion object {
    private const val FACTOR = 100.0
    val Zero = Amount(0L)

    private val enIn = locale("en", "IN")
    private val enSe = locale("en", "SE")
    private val deCh = locale("de", "CH")

    private fun locale(language: String, region: String) = Locale
      .Builder()
      .setLanguage(language)
      .setRegion(region)
      .build()
  }
}
