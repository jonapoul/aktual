package actual.budget.model

import kotlinx.datetime.LocalDate

sealed interface ReportDate {
  val date: LocalDate

  data class Month(val yearAndMonth: YearAndMonth) : ReportDate {
    override val date = yearAndMonth.date
    override fun toString() = yearAndMonth.toString()
  }

  data class Date(override val date: LocalDate) : ReportDate {
    override fun toString() = date.toString()
  }

  companion object {
    private const val SEPARATOR = "-"

    @Suppress("MagicNumber")
    fun parse(string: String): ReportDate = when (string.split(SEPARATOR).size) {
      2 -> Month(YearAndMonth(string))
      3 -> Date(LocalDate.parse(string))
      else -> error("Unexpected ReportDate '$string'")
    }
  }
}
