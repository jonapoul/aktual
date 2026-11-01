package aktual.budget.reports.vm

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class SummaryMetaTest {
  @Test
  fun `Reads upstream's defaults without content`() {
    val meta = SummaryReportMeta()
    assertThat(meta.summaryType).isEqualTo(Sum)
    assertThat(meta.allTimeDivisor).isFalse()
  }

  @Test
  fun `Setting a type keeps other content`() {
    val meta = SummaryReportMeta(content = """{"type":"sum","extra":1}""")

    val edited = meta.withSummaryType(AveragePerTransaction)

    assertThat(edited.content).isEqualTo("""{"type":"avgPerTransact","extra":1}""")
    assertThat(edited.summaryType).isEqualTo(AveragePerTransaction)
  }

  @Test
  fun `Setting an all-time divisor without content`() {
    val edited = SummaryReportMeta(name = "S").withAllTimeDivisor(allTime = true)

    assertThat(edited.content).isEqualTo("""{"divisorAllTimeDateRange":true}""")
    assertThat(edited.allTimeDivisor).isTrue()
  }

  @Test
  fun `Content written differently is the same report`() {
    val spaced = SummaryReportMeta(content = """{ "type": "sum" }""")
    val missing = SummaryReportMeta()
    val percent = SummaryReportMeta().withSummaryType(Percentage)

    assertThat(spaced.isSameAs(missing)).isTrue()
    assertThat(spaced.isSameAs(percent)).isFalse()
  }
}
