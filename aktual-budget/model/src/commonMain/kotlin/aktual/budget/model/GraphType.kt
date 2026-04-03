package aktual.budget.model

import alakazam.kotlin.SerializableByString

enum class GraphType(override val value: String) : SerializableByString {
  AreaGraph(value = "AreaGraph"),
  BarGraph(value = "BarGraph"),
  DonutGraph(value = "DonutGraph"),
  LineGraph(value = "LineGraph"),
  StackedBarGraph(value = "StackedBarGraph"),
  TableGraph(value = "TableGraph");

  override fun toString(): String = value
}
