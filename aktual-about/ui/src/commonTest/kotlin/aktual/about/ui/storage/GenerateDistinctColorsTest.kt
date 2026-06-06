package aktual.about.ui.storage

import aktual.core.theme.Colors
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import app.cash.burst.Burst
import app.cash.burst.burstValues
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

@Burst
class GenerateDistinctColorsTest {
  @Test
  fun `single color is generated`(colors: Colors = burstValues(LightColors, DarkColors)) {
    assertThat(generateDistinctColors(colors, count = 1)).hasSize(1)
  }

  @Test
  fun `requested count matches output size`(colors: Colors = burstValues(LightColors, DarkColors)) {
    for (count in 1..20) {
      assertThat(generateDistinctColors(colors, count = count)).hasSize(count)
    }
  }

  @Test
  fun `all colors are unique`(colors: Colors = burstValues(LightColors, DarkColors)) {
    for (count in 2..20) {
      val colors = generateDistinctColors(colors, count = count)
      assertThat(colors.distinct().size).isEqualTo(count)
    }
  }

  @Test
  fun `different parameters produce different colors`(
    colors: Colors = burstValues(LightColors, DarkColors)
  ) {
    val default = generateDistinctColors(colors, count = 5)
    val custom = generateDistinctColors(colors, count = 5, saturation = 0.5f, lightness = 0.25f)
    assertThat(default != custom).isEqualTo(true)
  }

  @Test
  fun `zero count throws`(colors: Colors = burstValues(LightColors, DarkColors)) {
    assertFailsWith<IllegalArgumentException> { generateDistinctColors(colors, count = 0) }
  }

  @Test
  fun `negative count throws`(colors: Colors = burstValues(LightColors, DarkColors)) {
    assertFailsWith<IllegalArgumentException> { generateDistinctColors(colors, count = -1) }
  }
}
