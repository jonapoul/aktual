package actual.budget.model

enum class GroupBy(private val value: String) {
  Account(value = "Account"),
  Category(value = "Category"),
  Group(value = "Group"),
  Interval(value = "Interval"),
  Payee(value = "Payee"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): GroupBy = entries
      .firstOrNull { it.value == string }
      ?: error("No GroupBy matching $string")
  }
}
