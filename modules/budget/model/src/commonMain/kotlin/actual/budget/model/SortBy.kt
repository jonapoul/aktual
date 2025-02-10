package actual.budget.model

enum class SortBy(private val value: String) {
  Asc(value = "asc"),
  Budget(value = "budget"),
  Desc(value = "desc"),
  Name(value = "name"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): SortBy = entries
      .firstOrNull { it.value == string }
      ?: error("No SortBy matching $string")
  }
}
