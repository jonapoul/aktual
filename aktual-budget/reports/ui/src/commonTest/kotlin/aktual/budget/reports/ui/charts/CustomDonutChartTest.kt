package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.core.theme.Colors
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import kotlin.test.Test

class CustomDonutChartTest {
  private val theme = Colors.Fallback

  @Test
  fun `Donut intervals take the qualitative colours`() {
    val data = previewCustomData(DonutGraph, Interval, Payment)
    val bars = customBars(data, names = List(data.intervals.size) { "$it" }, theme)
    assertThat(bars.map { it.color }).isEqualTo((0..5).map { theme.qualitative(it) })
  }

  @Test
  fun `Debts are kept, since they're drawn positive`() {
    val slices = listOf(bar("A", -10), bar("B", -5)).donutSlices(Debts)
    assertThat(slices.map { it.name }).containsExactly("A", "B")
  }

  @Test
  fun `Negative and empty totals are left out`() {
    val slices = listOf(bar("A", 10), bar("B", -5), bar("C", 0), bar("D", 3)).donutSlices(Total)
    assertThat(slices.map { it.name }).containsExactly("A", "D")
  }

  private fun bar(name: String, amount: Int) = CustomBar(name, Amount(amount.toDouble()), Red)
}
