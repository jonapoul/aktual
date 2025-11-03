/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

enum class GraphType(private val value: String) {
  AreaGraph(value = "AreaGraph"),
  BarGraph(value = "BarGraph"),
  DonutGraph(value = "DonutGraph"),
  LineGraph(value = "LineGraph"),
  StackedBarGraph(value = "StackedBarGraph"),
  TableGraph(value = "TableGraph"),
  ;

  override fun toString(): String = value
}
