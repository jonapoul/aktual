package actual.budget.model

enum class CustomReportMode(private val value: String) {
  Total(value = "total"),
  Time(value = "time"),
  ;

  override fun toString(): String = value
}
