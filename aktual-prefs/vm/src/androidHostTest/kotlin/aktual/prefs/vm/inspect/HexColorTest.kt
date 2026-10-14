package aktual.prefs.vm.inspect

import androidx.compose.ui.graphics.Color
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HexColorTest {
  @Test
  fun `Parses what toHexString writes`() {
    for (color in listOf(Color(0xFF2B8FED), Color(0x802B8FED), Color(0x00000000))) {
      assertThat(color.toHexString().parseHexColorOrNull()).isEqualTo(color)
    }
  }

  @Test
  fun `Accepts a missing hash and surrounding whitespace`() {
    assertThat(" 2b8fed ".parseHexColorOrNull()).isEqualTo(Color(0xFF2B8FED))
  }

  @Test
  fun `Rejects anything else`() {
    for (text in
      listOf("", "#", "#FFF", "#FFFFFFF", "#-FFFFF", "#+FFFFF", "#GGGGGG", "#FFFFFFFFFF")) {
      assertThat(text.parseHexColorOrNull()).isNull()
    }
  }
}
