package aktual.budget.tags.vm

import androidx.compose.ui.graphics.Color
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.Test

class TagColorTest {
  @Test
  fun `Parse 6-char hex`() {
    assertThat("#aabbcc".toColorOrNull()).isEqualTo(Color(0xFFAABBCC))
  }

  @Test
  fun `Parse without leading hash`() {
    assertThat("aabbcc".toColorOrNull()).isEqualTo(Color(0xFFAABBCC))
  }

  @Test
  fun `Parse is case-insensitive`() {
    assertThat("#AABBCC".toColorOrNull()).isEqualTo("#aabbcc".toColorOrNull())
  }

  @Test
  fun `Parse 3-char shorthand expands each nibble`() {
    assertThat("#abc".toColorOrNull()).isEqualTo(Color(0xFFAABBCC))
  }

  @Test
  fun `Parse 8-char ARGB normalises alpha away`() {
    // a semi-transparent input is stored opaque, so the swatch matches what gets persisted
    assertThat("#80FF0000".toColorOrNull()).isEqualTo(Color(0xFFFF0000))
  }

  @Test
  fun `Parse trims surrounding whitespace`() {
    assertThat("  #aabbcc  ".toColorOrNull()).isEqualTo(Color(0xFFAABBCC))
  }

  @Test
  fun `Parse rejects invalid lengths`() {
    assertThat("#ab".toColorOrNull()).isNull()
    assertThat("#abcde".toColorOrNull()).isNull()
  }

  @Test
  fun `Parse rejects non-hex characters`() {
    assertThat("#gggggg".toColorOrNull()).isNull()
  }

  @Test
  fun `Parse rejects empty string`() {
    assertThat("".toColorOrNull()).isNull()
  }

  @Test
  fun `Format is uppercase rrggbb`() {
    assertThat(Color(0xFFAABBCC).toHex()).isEqualTo("#AABBCC")
  }

  @Test
  fun `Format drops alpha`() {
    assertThat(Color(0x80FF0000).toHex()).isEqualTo("#FF0000")
  }

  @Test
  fun `Round-trips through parse and format`() {
    val hex = "#1976D2"
    assertThat(hex.toColorOrNull()?.toHex()).isEqualTo(hex)
  }
}
