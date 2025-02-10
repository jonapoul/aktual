package actual.budget.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * [value] has format like "2025-02"
 */
@JvmInline
value class YearAndMonth(private val value: String) {
  constructor(year: Int, month: Month) : this(LocalDate(year, month, dayOfMonth = 1))

  constructor(date: LocalDate) : this(
    value = date
      .toString()
      .split(SEPARATOR)
      .take(n = 2)
      .joinToString(SEPARATOR),
  )

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
  val month: Month get() = split[1].toInt().let(Month::of)
  val date: LocalDate get() = LocalDate(year, month, dayOfMonth = 1)

  override fun toString(): String = value

  companion object {
    private const val SEPARATOR = "-"
  }
}
