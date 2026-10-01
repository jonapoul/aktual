package aktual.budget.reports.ui.charts

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
  fun `Series outside the range keep their closest point`() {
    val points = points(1 to 10, 2 to 20, 3 to 30)
    assertThat(zoomedLine(points, zoom = 5.0..8.0)).isEqualTo(points(3 to 30))
    assertThat(zoomedColumns(points, zoom = 5.0..8.0)).isEqualTo(points(3 to 30))
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

  private fun points(vararg points: Pair<Int, Int>) = points.map { (x, y) ->
    ZoomPoint(x.toDouble(), y.toDouble())
  }

  private fun geometry() =
    ZoomGeometry(start = 0f, spacing = 10f, minX = 0.0, maxX = 10.0, xStep = 1.0)
}
