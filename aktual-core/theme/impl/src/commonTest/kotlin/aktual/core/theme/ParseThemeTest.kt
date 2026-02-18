package aktual.core.theme

import aktual.test.ThemeResponses
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import kotlin.test.Test

class ParseThemeTest {
  @Test
  fun `Parse CSS as theme`() {
    assertThat(parseTheme(ThemeResponses.ACTUAL_200)).isDataClassEqualTo(ShadesOfCoffeeTheme)
  }
}
