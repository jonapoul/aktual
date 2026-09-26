package aktual.budget.reports.vm.dashboard

import aktual.budget.db.Dashboard
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.UnsupportedReportMeta
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.prop
import kotlin.test.Test
import kotlinx.serialization.json.JsonObject

class DashboardItemDecoderTest {
  private val decoder = DashboardItemDecoder()

  @Test
  fun `Unknown widget type decodes as unsupported`() {
    val meta = JsonObject(emptyMap())
    val item = decoder.decode(widget(Unknown, meta))

    assertThat(item)
      .isNotNull()
      .prop(DashboardItem::meta)
      .isEqualTo(UnsupportedReportMeta(Unknown, meta, reason = "Unknown widget type"))
  }

  @Test
  fun `Invalid meta decodes as unsupported`() {
    val meta =
      JsonObject(mapOf("timeFrame" to JsonObject(mapOf("start" to JsonObject(emptyMap())))))
    val item = decoder.decode(widget(CashFlow, meta))

    assertThat(item).isNotNull().prop(DashboardItem::meta).isInstanceOf<UnsupportedReportMeta>()
  }

  private fun widget(type: WidgetType, meta: JsonObject) =
    Dashboard(
      id = WidgetId("abc"),
      type = type,
      width = 1,
      height = 1,
      x = 0,
      y = 0,
      meta = meta,
      tombstone = false,
      dashboard_page_id = null,
    )
}
