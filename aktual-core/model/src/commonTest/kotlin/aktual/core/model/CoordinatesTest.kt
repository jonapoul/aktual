package aktual.core.model

import assertk.assertThat
import assertk.assertions.isBetween
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

// Cases from upstream's location-utils.test.ts
class CoordinatesTest {
  @Test
  fun `Same place is no distance`() {
    assertThat(NYC.distanceTo(NYC).meters).isEqualTo(0.0)
  }

  @Test
  fun `Long distance`() {
    val philly = Coordinates(39.9526, -75.1652)
    assertThat(NYC.distanceTo(philly).meters).isBetween(122_000.0, 136_000.0)
  }

  @Test
  fun `Short distance`() {
    assertThat(NYC.distanceTo(Coordinates(40.7137, -74.0068)).meters).isBetween(90.0, 130.0)
  }

  @Test
  fun `Pole to pole`() {
    val distance = Coordinates(89.0, 0.0).distanceTo(Coordinates(-89.0, 0.0))
    assertThat(distance.meters).isBetween(19_000_000.0, 20_100_000.0)
  }

  @Test
  fun `Across the antimeridian takes the short way`() {
    val distance = Coordinates(51.5074, 179.0).distanceTo(Coordinates(51.5074, -179.0))
    assertThat(distance.meters).isBetween(1.0, 1_000_000.0)
  }

  @Test
  fun `Out of range or missing values are null`() {
    assertThat(Coordinates.orNull(91.0, 0.0)).isNull()
    assertThat(Coordinates.orNull(0.0, -181.0)).isNull()
    assertThat(Coordinates.orNull(null, 0.0)).isNull()
    assertThat(Coordinates.orNull(1.0, 2.0)).isEqualTo(Coordinates(1.0, 2.0))
  }

  private companion object {
    val NYC = Coordinates(40.7128, -74.006)
  }
}
