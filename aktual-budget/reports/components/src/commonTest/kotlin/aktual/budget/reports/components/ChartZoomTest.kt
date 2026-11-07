package aktual.budget.reports.components

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ChartZoomTest {
  @Test
  fun `No zoom keeps every point`() {
    val points = points(1 to 1, 2 to 2, 3 to 3)
    assertThat(zoomedLine(points, zoom = null)).isEqualTo(points)
    assertThat(zoomedColumns(points, zoom = null)).isEqualTo(points)
  }

  @Test
  fun `Lines keep points in range`() {
    val points = points(1 to 10, 2 to 20, 3 to 30, 4 to 40)
    assertThat(zoomedLine(points, zoom = 2.0..3.0)).isEqualTo(points(2 to 20, 3 to 30))
  }

  @Test
  fun `Lines are interpolated at range edges without points`() {
    val points = points(0 to 0, 4 to 40, 8 to 0)
    assertThat(zoomedLine(points, zoom = 2.0..6.0)).isEqualTo(points(2 to 20, 4 to 40, 6 to 20))
  }

  @Test
  fun `Lines are interpolated when no points are in range`() {
    val points = points(0 to 0, 10 to 100)
    assertThat(zoomedLine(points, zoom = 2.0..4.0)).isEqualTo(points(2 to 20, 4 to 40))
  }

  @Test
  fun `Series outside the range keep their closest point, zeroed to not affect the y range`() {
    val points = points(1 to 10, 2 to 20, 3 to 30)
    assertThat(zoomedLine(points, zoom = 5.0..8.0)).isEqualTo(points(3 to 0))
    assertThat(zoomedColumns(points, zoom = 5.0..8.0)).isEqualTo(points(3 to 0))
  }

  @Test
  fun `Columns aren't interpolated`() {
    val points = points(0 to 0, 4 to 40, 8 to 0)
    assertThat(zoomedColumns(points, zoom = 2.0..6.0)).isEqualTo(points(4 to 40))
  }

  @Test
  fun `Pointer positions snap to the nearest x value`() {
    val geometry = geometry()
    assertThat(geometry.toX(14f)).isEqualTo(1.0)
    assertThat(geometry.toX(16f)).isEqualTo(2.0)
  }

  @Test
  fun `Pointer positions outside the chart clamp to the x range`() {
    val geometry = geometry()
    assertThat(geometry.toX(-50f)).isEqualTo(0.0)
    assertThat(geometry.toX(500f)).isEqualTo(10.0)
  }

  @Test
  fun `Right to left layouts map from the right edge`() {
    val geometry = ZoomGeometry(start = 100f, spacing = -10f, minX = 0.0, maxX = 10.0, xStep = 1.0)
    assertThat(geometry.toX(80f)).isEqualTo(2.0)
  }

  @Test
  fun `Selection range is ordered whichever way it was dragged`() {
    assertThat(geometry().range(from = 60f, to = 20f)).isEqualTo(2.0..6.0)
  }

  @Test
  fun `Selection within one x value doesn't zoom`() {
    assertThat(geometry().range(from = 21f, to = 24f)).isNull()
  }

  @Test
  fun `Selection across everything doesn't zoom`() {
    assertThat(geometry().range(from = -10f, to = 200f)).isNull()
  }

  @Test
  fun `Pinching out keeps the value under the fingers in place`() {
    // fingers at x = 5, spread to twice the distance
    assertThat(geometry().pinched(FULL, from = 50f, to = 50f, zoom = 2f)).isEqualTo(3.0..8.0)
  }

  @Test
  fun `Pinching follows the fingers as they move`() {
    // the value at 5 is dragged to where 2 was, then zoomed in twice
    assertThat(geometry().pinched(FULL, from = 50f, to = 20f, zoom = 2f)).isEqualTo(4.0..9.0)
  }

  @Test
  fun `Pinching stays within the chart`() {
    assertThat(geometry().pinched(FULL, from = 10f, to = 50f, zoom = 2f)).isEqualTo(0.0..5.0)
    assertThat(geometry().pinched(FULL, from = 50f, to = 0f, zoom = 2f)).isEqualTo(5.0..10.0)
  }

  @Test
  fun `Pinching in shows at least one x step`() {
    assertThat(geometry().pinched(FULL, from = 50f, to = 50f, zoom = 100f)).isEqualTo(5.0..6.0)
  }

  @Test
  fun `Pinching out past everything resets the zoom`() {
    val zoomed = ZoomGeometry(start = 0f, spacing = 20f, minX = 2.0, maxX = 7.0, xStep = 1.0)
    assertThat(zoomed.pinched(FULL, from = 50f, to = 50f, zoom = 0.4f)).isNull()
  }

  @Test
  fun `Pinching out from a zoomed range widens it`() {
    val zoomed = ZoomGeometry(start = 0f, spacing = 20f, minX = 2.0, maxX = 6.0, xStep = 1.0)
    assertThat(zoomed.pinched(FULL, from = 40f, to = 40f, zoom = 0.5f)).isEqualTo(0.0..8.0)
  }

  private fun points(vararg points: Pair<Int, Int>) = points.map { (x, y) ->
    ZoomPoint(x.toDouble(), y.toDouble())
  }

  private fun geometry() =
    ZoomGeometry(start = 0f, spacing = 10f, minX = 0.0, maxX = 10.0, xStep = 1.0)

  private companion object {
    val FULL = 0.0..10.0
  }
}
