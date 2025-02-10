package actual.budget.model

enum class DateRangeType(private val value: String) {
  ThisWeek(value = "This week"),
  LastWeek(value = "Last week"),
  ThisMonth(value = "This month"),
  LastMonth(value = "Last month"),
  Last3Months(value = "Last 3 months"),
  Last6Months(value = "Last 6 months"),
  Last12Months(value = "Last 12 months"),
  YearToDate(value = "Year to date"),
  LastYear(value = "Last year"),
  AllTime(value = "All time"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): DateRangeType = entries
      .firstOrNull { it.value == string }
      ?: error("No DateRangeType matching $string")
  }
}
