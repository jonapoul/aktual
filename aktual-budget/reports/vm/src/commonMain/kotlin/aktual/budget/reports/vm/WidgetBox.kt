package aktual.budget.reports.vm

import aktual.budget.model.WidgetId

// A widget's place and size on the browser's grid
internal data class WidgetBox(
  val id: WidgetId,
  val x: Long,
  val y: Long,
  val width: Long,
  val height: Long,
)
