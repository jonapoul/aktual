package aktual.core.theme

import androidx.compose.ui.graphics.Color
import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import kotlin.test.Test

class ParseColorTest {
  // --- hex ---

  @Test
  fun `hex 6-char`() {
    // --color-pageBackground: #e2d8cf
    assertThat("#e2d8cf".parseColor()).isEqualTo(Color(0xFFe2d8cf))
  }

  @Test
  fun `hex 6-char mixed case`() {
    // --color-pageTextDark: #243B53
    assertThat("#243B53".parseColor()).isEqualTo(Color(0xFF243B53))
  }

  @Test
  fun `hex 3-char expands correctly`() {
    assertThat("#fff".parseColor()).isEqualTo(Color(0xFFFFFFFF))
  }

  @Test
  fun `hex 3-char non-white`() {
    assertThat("#abc".parseColor()).isEqualTo(Color(0xFFaabbcc))
  }

  // --- rgba ---

  @Test
  fun `rgba with leading-dot alpha`() {
    // --color-mobileHeaderTextHover: rgba(200, 200, 200, .15)
    assertThat("rgba(200, 200, 200, .15)".parseColor())
      .isEqualTo(Color(red = 200 / 255f, green = 200 / 255f, blue = 200 / 255f, alpha = 0.15f))
  }

  @Test
  fun `rgba black semi-transparent`() {
    // --color-overlayBackground: rgba(0, 0, 0, 0.3)
    assertThat("rgba(0, 0, 0, 0.3)".parseColor())
      .isEqualTo(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.3f))
  }

  @Test
  fun `rgba light overlay`() {
    // --color-buttonMenuBackgroundHover: rgba(200, 200, 200, 0.25)
    assertThat("rgba(200, 200, 200, 0.25)".parseColor())
      .isEqualTo(Color(red = 200 / 255f, green = 200 / 255f, blue = 200 / 255f, alpha = 0.25f))
  }

  @Test
  fun `rgba dark overlay lighter`() {
    // --color-buttonNormalShadow: rgba(0, 0, 0, 0.2)
    assertThat("rgba(0, 0, 0, 0.2)".parseColor())
      .isEqualTo(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.2f))
  }

  @Test
  fun `rgb with percentages`() {
    assertThat("rgb(100%, 50%, 0%)".parseColor())
      .isEqualTo(Color(red = 1f, green = 0.5f, blue = 0f))
  }

  @Test
  fun `rgb with alpha`() {
    assertThat("rgb(0, 0, 0, 0.3)".parseColor())
      .isEqualTo(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.3f))
  }

  @Test
  fun `rgba without alpha`() {
    assertThat("rgba(255, 0, 0)".parseColor()).isEqualTo(Color(0xFFFF0000))
  }

  @Test
  fun `uppercase function name`() {
    assertThat("RGB(255, 0, 0)".parseColor()).isEqualTo(Color(0xFFFF0000))
  }

  // --- hsl ---

  @Test
  fun `hsl red`() {
    assertThat("hsl(0, 100%, 50%)".parseColor()).isEqualTo(Color(0xFFFF0000))
  }

  @Test
  fun `hsla with alpha`() {
    assertThat("hsla(0, 100%, 50%, 0.5)".parseColor())
      .isEqualTo(Color(red = 1f, green = 0f, blue = 0f, alpha = 0.5f))
  }

  // --- transparent ---

  @Test
  fun transparent() {
    // --color-buttonMenuBackground: transparent
    assertThat("transparent".parseColor()).isEqualTo(Transparent)
  }

  @Test
  fun `uppercase transparent`() {
    assertThat("TRANSPARENT".parseColor()).isEqualTo(Transparent)
  }

  // --- errors ---

  @Test
  fun `unrecognized format throws ColorParseException`() {
    assertFailure { "hotpink".parseColor() }.isInstanceOf<ColorParseException>()
  }

  @Test
  fun `invalid hex length throws ColorParseException`() {
    assertFailure { "#12345".parseColor() }.isInstanceOf<ColorParseException>()
  }

  @Test
  fun `unrecognized format returns null when parsing or null`() {
    assertThat("inherit".parseColorOrNull()).isNull()
    assertThat("#12345".parseColorOrNull()).isNull()
  }
}
