package actual.budget.model

enum class CustomReportMode(private val value: String) {
  Total(value = "total"),
  Time(value = "time"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): CustomReportMode = entries
      .firstOrNull { it.value == string }
      ?: error("No CustomReportMode matching $string")
  }
}
