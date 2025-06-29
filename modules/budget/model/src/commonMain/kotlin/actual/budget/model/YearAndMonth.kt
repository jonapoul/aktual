package actual.budget.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.number

/**
 * [value] has format like "2025-02"
 */
@JvmInline
value class YearAndMonth(private val value: String) : Comparable<YearAndMonth> {
  // Comes like "202402" -> Feb 2024
  constructor(long: Long) : this(long.toLocalDate())

  constructor(year: Int, month: Month) : this(LocalDate(year, month, day = 1))

  constructor(date: LocalDate) : this(value = "%04d-%02d".format(date.year, date.month.number))

  private val split get() = value.split(SEPARATOR)

  init {
    val errorMessage by lazy { "Invalid LocalMonth '$value'" }
    require(split.size == 2) { errorMessage }
    val year = split[0]
    val month = split[1]
    require(year.all { it.isDigit() })
    require(month.all { it.isDigit() })
  }

  val year: Int get() = split[0].toInt()
  val month: Month get() = split[1].toInt().let(::Month)
  val date: LocalDate get() = LocalDate(year, month, day = 1)

  override fun toString(): String = value

  override fun compareTo(other: YearAndMonth): Int = value.compareTo(other.value)

  fun toLong(): Long = (year * FACTOR.toLong()) + month.number

  fun monthNumber(): Long = (year * MONTHS_PER_YEAR) + month.number

  companion object {
    fun fromMonthNumber(number: Long) = YearAndMonth(
      year = (number / MONTHS_PER_YEAR).toInt(),
      month = Month((number % MONTHS_PER_YEAR).toInt()),
    )

    private const val SEPARATOR = "-"

    private const val FACTOR = 100
    private const val MONTHS_PER_YEAR = 12L

    private fun Long.toLocalDate(): LocalDate {
      val month = Month(toInt() % FACTOR)
      val year = toInt() / FACTOR
      return LocalDate(year, month, day = 1)
    }
  }
}
