package aktual.budget.reports.ui.charts

import aktual.budget.model.Amount
import aktual.budget.reports.vm.SankeyColor
import aktual.budget.reports.vm.SankeyData
import aktual.budget.reports.vm.SankeyLabel
import aktual.budget.reports.vm.SankeyLink
import aktual.budget.reports.vm.SankeyNode
import aktual.core.model.Percent
import assertk.all
import assertk.assertThat
import assertk.assertions.each
import assertk.assertions.isCloseTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThanOrEqualTo
import assertk.assertions.isLessThanOrEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth

class SankeyLayoutTest {
  @Test
  fun `No nodes gives an empty layout`() {
    val layout = layout(data(nodes = emptyList(), links = emptyList()))

    assertThat(layout.nodes).isEmpty()
    assertThat(layout.links).isEmpty()
  }

  @Test
  fun `Zero sized canvas gives an empty layout`() {
    val layout = layout(SIMPLE, width = 0f)

    assertThat(layout.nodes).isEmpty()
  }

  @Test
  fun `Columns are spread evenly from the left edge to the right edge`() {
    val layout = layout(SIMPLE)

    assertThat(layout.nodes.map { it.left }).isEqualTo(listOf(0f, 496f, 496f, 992f))
    assertThat(layout.nodes.map { it.width }).each { it.isEqualTo(NODE_WIDTH) }
  }

  @Test
  fun `A single column sits at the left edge`() {
    val layout = layout(data(nodes = listOf(node(0, 100), node(0, 50)), links = emptyList()))

    assertThat(layout.nodes.map { it.left }).isEqualTo(listOf(0f, 0f))
  }

  @Test
  fun `Node heights are proportional to their values`() {
    val layout = layout(SIMPLE)

    val (a, b) = layout.nodes.subList(1, 3)
    assertThat(a.height / b.height).isCloseTo(3f, TOLERANCE)
  }

  @Test
  fun `The fullest column fills the height, including padding`() {
    val layout = layout(SIMPLE)

    val column = layout.nodes.subList(1, 3)
    val used = column.sumOf { it.height.toDouble() }.toFloat() + PADDING
    assertThat(used).isCloseTo(HEIGHT, TOLERANCE)
  }

  @Test
  fun `Nodes stay in bounds, keep their order and don't overlap`() {
    val layout = layout(MANY)

    for (column in MANY.nodes.indices.groupBy { MANY.nodes[it].column }.values) {
      val rects = column.map { layout.nodes[it] }
      assertThat(rects).each { rect ->
        rect.transform { it.top }.isGreaterThanOrEqualTo(-TOLERANCE)
        rect.transform { it.bottom }.isLessThanOrEqualTo(HEIGHT + TOLERANCE)
        rect.transform { it.height }.isGreaterThanOrEqualTo(0f)
      }
      rects.zipWithNext().forEach { (above, below) ->
        assertThat(below.top).isGreaterThanOrEqualTo(above.bottom - TOLERANCE)
      }
    }
  }

  @Test
  fun `Padding shrinks when there are too many nodes to fit`() {
    val layout = layout(MANY, height = 100f)

    val column = MANY.nodes.indices.filter { MANY.nodes[it].column == 1 }.map { layout.nodes[it] }
    // At most 40% of the height goes on padding, so 29 gaps share 40px
    val gaps = column.zipWithNext { above, below -> below.top - above.bottom }
    assertThat(gaps).each { it.isCloseTo(40f / 29, TOLERANCE) }
    assertThat(column.first().top).isCloseTo(0f, TOLERANCE)
    assertThat(column.last().bottom).isCloseTo(100f, TOLERANCE)
  }

  @Test
  fun `Link bands match their values and start and end inside their nodes`() {
    val layout = layout(SIMPLE)
    val scale = layout.nodes[0].height / SIMPLE.nodes[0].value.toLong()

    SIMPLE.links.forEachIndexed { i, link ->
      val band = layout.links[i]
      val source = layout.nodes[link.source]
      val target = layout.nodes[link.target]
      assertThat(band).all {
        transform { it.thickness }.isCloseTo(link.value.toLong() * scale, TOLERANCE)
        transform { it.x0 }.isEqualTo(source.right)
        transform { it.x1 }.isEqualTo(target.left)
        transform { it.y0 - it.thickness / 2 >= source.top - TOLERANCE }.isTrue()
        transform { it.y0 + it.thickness / 2 <= source.bottom + TOLERANCE }.isTrue()
        transform { it.y1 - it.thickness / 2 >= target.top - TOLERANCE }.isTrue()
        transform { it.y1 + it.thickness / 2 <= target.bottom + TOLERANCE }.isTrue()
      }
    }
  }

  @Test
  fun `Outgoing bands are stacked without gaps from the top of the node`() {
    val layout = layout(SIMPLE)

    val (first, second) = layout.links.subList(0, 2).sortedBy { it.y0 }
    assertThat(first.y0 - first.thickness / 2).isCloseTo(layout.nodes[0].top, TOLERANCE)
    assertThat(second.y0 - second.thickness / 2)
      .isCloseTo(first.y0 + first.thickness / 2, TOLERANCE)
  }

  private fun layout(
    data: SankeyData,
    width: Float = WIDTH,
    height: Float = HEIGHT,
  ) = layoutSankey(data, width, height, NODE_WIDTH, PADDING)

  private companion object {
    const val WIDTH = 1000f
    const val HEIGHT = 500f
    const val NODE_WIDTH = 8f
    const val PADDING = 20f
    const val TOLERANCE = 0.01f

    // Income splits into two categories, which both flow into one account
    val SIMPLE =
      data(
        nodes = listOf(node(0, 400), node(1, 300), node(1, 100), node(2, 400)),
        links = listOf(link(0, 1, 300), link(0, 2, 100), link(1, 3, 300), link(2, 3, 100)),
      )

    // One source fanning out to 30 targets of varying sizes
    val MANY =
      data(
        nodes = listOf(node(0, 4650)) + (1..30).map { node(1, it * 10L) },
        links = (1..30).map { link(0, it, it * 10L) },
      )
  }
}

private fun node(column: Int, value: Long) =
  SankeyNode(
    key = "$column-$value",
    label = SankeyLabel.Text("$column-$value"),
    column = column,
    value = Amount(value),
    percent = Percent(0),
    color = SankeyColor.Palette(0),
  )

private fun link(source: Int, target: Int, value: Long) =
  SankeyLink(source, target, Amount(value), SankeyColor.Palette(0))

private fun data(nodes: List<SankeyNode>, links: List<SankeyLink>) =
  SankeyData(
    title = null,
    start = YearMonth(2026, JANUARY),
    end = YearMonth(2026, JANUARY),
    showPercentages = false,
    nodes = nodes.toImmutableList(),
    links = links.toImmutableList(),
  )
