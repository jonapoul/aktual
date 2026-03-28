package aktual.about.ui.storage

import aktual.core.theme.DarkTheme
import aktual.core.theme.LightTheme
import aktual.core.theme.Theme
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
  fun `single color is generated`(theme: Theme = burstValues(LightTheme, DarkTheme)) {
    assertThat(generateDistinctColors(theme, count = 1)).hasSize(1)
  }

  @Test
  fun `requested count matches output size`(theme: Theme = burstValues(LightTheme, DarkTheme)) {
    for (count in 1..20) {
      assertThat(generateDistinctColors(theme, count = count)).hasSize(count)
    }
  }

  @Test
  fun `all colors are unique`(theme: Theme = burstValues(LightTheme, DarkTheme)) {
    for (count in 2..20) {
      val colors = generateDistinctColors(theme, count = count)
      assertThat(colors.distinct().size).isEqualTo(count)
    }
  }

  @Test
  fun `different parameters produce different colors`(
    theme: Theme = burstValues(LightTheme, DarkTheme)
  ) {
    val default = generateDistinctColors(theme, count = 5)
    val custom = generateDistinctColors(theme, count = 5, saturation = 0.5f, lightness = 0.25f)
    assertThat(default != custom).isEqualTo(true)
  }

  @Test
  fun `zero count throws`(theme: Theme = burstValues(LightTheme, DarkTheme)) {
    assertFailsWith<IllegalArgumentException> { generateDistinctColors(theme, count = 0) }
  }

  @Test
  fun `negative count throws`(theme: Theme = burstValues(LightTheme, DarkTheme)) {
    assertFailsWith<IllegalArgumentException> { generateDistinctColors(theme, count = -1) }
  }
}
