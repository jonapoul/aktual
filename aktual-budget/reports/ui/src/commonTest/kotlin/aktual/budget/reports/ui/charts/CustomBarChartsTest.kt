package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.CustomData
import aktual.budget.reports.vm.CustomGroup
import aktual.core.theme.Colors
import androidx.compose.ui.text.AnnotatedString
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test

class CustomBarChartsTest {
  private val theme = Colors.Fallback

  @Test
  fun `Groups keep their order and take the qualitative colours`() {
    val data = previewCustomData(BarGraph, Category, Payment)
    val bars = customBars(data, data.names(), theme)

    assertThat(bars.map { it.name })
      .containsExactly("Rent", "Holiday", "Groceries", "Eating out", "Transport")
    assertThat(bars.map { it.amount }).isEqualTo(amounts(-7250, -3050, -2525, -820, -585))
    assertThat(bars.map { it.color }).isEqualTo((0..4).map { theme.qualitative(it) })
  }

  @Test
  fun `Qualitative colours wrap around`() {
    assertThat(theme.qualitative(9)).isEqualTo(theme.qualitative(0))
    assertThat(theme.qualitative(10)).isEqualTo(theme.qualitative(1))
  }

  @Test
  fun `Net intervals are coloured by sign`() {
    val data = previewCustomData(BarGraph, Interval, Net)
    val bars = customBars(data, names = List(data.intervals.size) { "$it" }, theme)

    assertThat(bars[0]).all {
      prop(CustomBar::amount).isEqualTo(Amount(1360.0))
      prop(CustomBar::color).isEqualTo(theme.reportsNumberPositive)
    }
    assertThat(bars[5]).all {
      prop(CustomBar::amount).isEqualTo(Amount(-930.0))
      prop(CustomBar::color).isEqualTo(theme.reportsNumberNegative)
    }
  }

  @Test
  fun `Payment intervals are always negative coloured`() {
    val data = previewCustomData(BarGraph, Interval, Payment)
    val bars = customBars(data, names = List(data.intervals.size) { "$it" }, theme)
    assertThat(bars.map { it.color }.distinct()).containsExactly(theme.reportsNumberNegative)
  }

  @Test
  fun `Debts are drawn above the axis`() {
    val amount = Amount(-12.0)
    assertThat(amount.plotted(Debts)).isEqualTo(Amount(12.0))
    assertThat(amount.plotted(NetDebts)).isEqualTo(Amount(12.0))
    assertThat(amount.plotted(Total)).isEqualTo(amount)
    assertThat(amount.plotted(Assets)).isEqualTo(amount)
    assertThat(amount.plotted(NetAssets)).isEqualTo(amount)
  }

  @Test
  fun `Stacks hold each group's amount per interval in legend order`() {
    val data = previewCustomData(StackedBarGraph, Category, Payment)
    val legend = customBars(data, data.names(), theme)
    val stacks = customStacks(data, legend)

    assertThat(stacks.size).isEqualTo(6)
    val march = stacks[2]
    assertThat(march.map { it.name }).isEqualTo(legend.map { it.name })
    assertThat(march.map { it.color }).isEqualTo(legend.map { it.color })
    assertThat(march.map { it.amount }).isEqualTo(amounts(1200, 650, 455, 210, 110))
  }

  @Test
  fun `Stack series run bottom up, so the first group is drawn last`() {
    val stacks = listOf(listOf(bar("A", 1), bar("B", 2)), listOf(bar("A", 3), bar("B", 4)))
    assertThat(stackSeries(stacks)).containsExactly(listOf(2.0, 4.0), listOf(1.0, 3.0))
  }

  @Test
  fun `No stacks give no series`() {
    assertThat(stackSeries(emptyList())).isEmpty()
  }

  @Test
  fun `Marker skips empty groups and ends with the total`() {
    val text = markerText(listOf(bar("A", 10), bar("B", 0), bar("C", 5)))
    assertThat(text)
      .isEqualTo(
        """
        Jan 25
        A: 10.00
        C: 5.00
        Total: 15.00
        """
          .trimIndent(),
      )
  }

  @Test
  fun `Marker cuts off rows past the limit`() {
    val stack = (1..MAX_MARKER_ROWS + 2).map { bar("G$it", it) }
    val lines = markerText(stack).lines()

    assertThat(lines.size).isEqualTo(MAX_MARKER_ROWS + 3)
    assertThat(lines[MAX_MARKER_ROWS]).isEqualTo("G$MAX_MARKER_ROWS: $MAX_MARKER_ROWS.00")
    assertThat(lines[MAX_MARKER_ROWS + 1]).isEqualTo("…")
    assertThat(lines.last()).isEqualTo("Total: 78.00")
  }

  @Test
  fun `Marker lines match the text of a full stack`() {
    for (groups in listOf(1, 5, MAX_MARKER_ROWS, MAX_MARKER_ROWS + 2)) {
      val stack = (1..groups).map { bar("G$it", it) }
      assertThat(markerLines(groups)).isEqualTo(markerText(stack).lines().size)
    }
  }

  private fun CustomData.names() = groups.map { (it.group as CustomGroup.Category).name }

  private fun bar(name: String, amount: Int) = CustomBar(name, Amount(amount.toDouble()), Red)

  private fun markerText(stack: List<CustomBar>) =
    stackedMarkerText("Jan 25", stack, "Total") { AnnotatedString(it.toString()) }.text

  private fun amounts(vararg values: Int) = values.map { Amount(it.toDouble()) }
}
