package aktual.budget.reports.vm

import aktual.budget.model.ConditionOp
import aktual.budget.model.WidgetType
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.OCTOBER
import kotlinx.datetime.YearMonth
import kotlinx.serialization.json.Json

class ReportMetaSerializationTest {
  private val json = Json

  @Test
  fun `TimeFrame parses a full ISO date by truncating to year-month`() {
    // Regression: upstream stores start/end as full dates like "2011-10-05", which used to crash
    // the dashboard because the field is typed as a YearMonth.
    val decoded =
      json.decodeFromString(
        TimeFrame.serializer(),
        """{"start":"2011-10-05","end":"2025-07-31","mode":"static"}""",
      )

    assertThat(decoded.start).isEqualTo(YearMonth(2011, OCTOBER))
    assertThat(decoded.end).isEqualTo(YearMonth(2025, JULY))
    assertThat(decoded.mode).isEqualTo(TimeFrameMode.Static)
  }

  @Test
  fun `TimeFrame parses a plain year-month`() {
    val decoded =
      json.decodeFromString(
        TimeFrame.serializer(),
        """{"start":"2011-10","end":"2025-07","mode":"sliding-window"}""",
      )

    assertThat(decoded.start).isEqualTo(YearMonth(2011, OCTOBER))
    assertThat(decoded.mode).isEqualTo(TimeFrameMode.SlidingWindow)
  }

  @Test
  fun `FormulaReportMeta with a full-date query time frame decodes`() {
    // Mirrors the exact shape from the original crash: a formula query whose nested timeFrame
    // holds full dates.
    val element =
      json.parseToJsonElement(
        """
        {
          "queries": {
            "a": {
              "conditions": [],
              "conditionsOp": "and",
              "timeFrame": {"start":"2011-10-05","end":"2025-07-31","mode":"static"}
            }
          }
        }
        """
          .trimIndent()
      )

    val decoded = json.decodeFromJsonElement(ReportMeta.serializer(WidgetType.Formula), element)

    val formula = decoded as FormulaReportMeta
    assertThat(formula.queries.getValue("a").timeFrame?.start).isEqualTo(YearMonth(2011, OCTOBER))
    assertThat(formula.queries.getValue("a").conditionsOp).isEqualTo(ConditionOp.And)
  }
}
