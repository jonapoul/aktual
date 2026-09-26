package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.UnsupportedReportMeta
import dev.zacsweers.metro.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import logcat.logcat

@Inject
internal class DashboardItemDecoder {
  fun decode(widget: Dashboard): DashboardItem? {
    val type = widget.type ?: return null
    // Upstream adds widgets with null meta, and each card falls back to its defaults
    val meta = widget.meta ?: JsonObject(emptyMap())
    return DashboardItem(
      id = widget.id,
      width = widget.width?.toInt() ?: 0,
      height = widget.height?.toInt() ?: 0,
      x = widget.x?.toInt() ?: 0,
      y = widget.y?.toInt() ?: 0,
      meta = decodeMeta(type, meta),
    )
  }

  private fun decodeMeta(type: WidgetType, meta: JsonObject): ReportMeta =
    try {
      Json.decodeFromJsonElement(ReportMeta.serializer(type), meta)
    } catch (e: SerializationException) {
      logcat.e(e) { "Failed to deserialize $type report meta: $meta" }
      UnsupportedReportMeta(type, meta, reason = e.message ?: e.toString())
    }
}
