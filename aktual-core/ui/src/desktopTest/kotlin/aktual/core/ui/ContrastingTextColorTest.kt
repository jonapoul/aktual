package aktual.core.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class ContrastingTextColorTest {
  @Test
  fun `Opaque colors ignore the background`() {
    assertEquals(Color.Black, Color(0xFFFFFF00).contrastingTextColor(background = Black))
    assertEquals(Color.White, Color(0xFF142726).contrastingTextColor(background = White))
  }

  @Test
  fun `Translucent dark color on light background gives black text`() {
    assertEquals(Color.Black, Color(0x30142726).contrastingTextColor(background = White))
  }

  @Test
  fun `Translucent dark color on dark background gives white text`() {
    assertEquals(Color.White, Color(0x30142726).contrastingTextColor(background = Black))
  }
}
