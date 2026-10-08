package aktual.core.theme

import assertk.assertThat
import assertk.assertions.isFalse
import kotlin.test.Test

class ColorsTest {
  @Test
  fun `Built-in themes have no alternate row colour`() {
    Colors.Defaults.forEach { colors -> assertThat(colors.hasAlternateRowColour).isFalse() }
  }
}
