package actual.budget.model

enum class GraphType(val value: String) {
  AreaGraph(value = "AreaGraph"),
  BarGraph(value = "BarGraph"),
  DonutGraph(value = "DonutGraph"),
  LineGraph(value = "LineGraph"),
  StackedBarGraph(value = "StackedBarGraph"),
  TableGraph(value = "TableGraph"),
  ;

  override fun toString(): String = value

  companion object {
    fun fromString(string: String): GraphType = entries
      .firstOrNull { it.value == string }
      ?: error("No GraphType matching $string")
  }
}
