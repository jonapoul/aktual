package actual.budget.model

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
sealed interface ReportDate {
  val date: LocalDate

  /**
   * Like "2011-10"
   */
  data class Month(val yearAndMonth: YearAndMonth) : ReportDate {
    override val date = yearAndMonth.date
    override fun toString() = yearAndMonth.toString()
  }

  /**
   * Like "2023-11-01"
   */
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
