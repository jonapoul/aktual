package aktual.budget.reports.vm

import aktual.budget.model.WidgetId
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class DashboardLayoutTest {
  @Test
  fun `Same-sized widgets swap slots`() {
    val boxes = listOf(box(A, x = 0, y = 0), box(B, x = 4, y = 0), box(C, x = 0, y = 2))

    assertThat(reorderLayout(boxes, listOf(C, A, B)))
      .containsExactly(box(C, x = 0, y = 0), box(A, x = 4, y = 0), box(B, x = 0, y = 2))
  }

  @Test
  fun `Gaps in the grid are kept`() {
    val boxes = listOf(box(A, x = 0, y = 0), box(B, x = 8, y = 4))

    assertThat(reorderLayout(boxes, listOf(B, A)))
      .containsExactly(box(B, x = 0, y = 0), box(A, x = 8, y = 4))
  }

  @Test
  fun `Widgets keep their size`() {
    val boxes = listOf(box(A, x = 0, y = 0, height = 4), box(B, x = 4, y = 0))

    assertThat(reorderLayout(boxes, listOf(B, A)))
      .containsExactly(box(B, x = 0, y = 0), box(A, x = 4, y = 0, height = 4))
  }

  @Test
  fun `Pack in the new order when a widget would overlap another`() {
    val boxes = listOf(box(A, x = 0, y = 0, width = 12), box(B, x = 0, y = 2), box(C, x = 4, y = 2))

    assertThat(reorderLayout(boxes, listOf(B, A, C)))
      .containsExactly(box(B, x = 0, y = 0), box(A, x = 0, y = 2, width = 12), box(C, x = 0, y = 4))
  }

  @Test
  fun `Pack in the new order when a widget would go past the right edge`() {
    val boxes = listOf(box(A, x = 0, y = 0, width = 8), box(B, x = 8, y = 0))

    assertThat(reorderLayout(boxes, listOf(B, A)))
      .containsExactly(box(B, x = 0, y = 0), box(A, x = 4, y = 0, width = 8))
  }

  @Test
  fun `Widgets missing from the order go after the ordered ones`() {
    val boxes = listOf(box(A, x = 0, y = 0), box(B, x = 4, y = 0), box(C, x = 8, y = 0))

    assertThat(reorderLayout(boxes, listOf(C, A)))
      .containsExactly(box(C, x = 0, y = 0), box(A, x = 4, y = 0), box(B, x = 8, y = 0))
  }

  @Test
  fun `Unknown widgets in the order are ignored`() {
    val boxes = listOf(box(A, x = 0, y = 0), box(B, x = 4, y = 0))

    assertThat(reorderLayout(boxes, listOf(B, WidgetId("gone"), A)))
      .containsExactly(box(B, x = 0, y = 0), box(A, x = 4, y = 0))
  }

  private fun box(id: WidgetId, x: Long, y: Long, width: Long = 4, height: Long = 2) =
    WidgetBox(id, x, y, width, height)

  private companion object {
    val A = WidgetId("a")
    val B = WidgetId("b")
    val C = WidgetId("c")
  }
}
