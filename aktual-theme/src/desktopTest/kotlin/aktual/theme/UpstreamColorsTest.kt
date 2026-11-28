package aktual.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import assertk.assertThat
import assertk.assertions.isEmpty
import kotlin.test.Test

// Checks the built-in themes against upstream's resolved colours, which the upstream theme tracker
// keeps in last-known-resolved.txt. When the tracker's PR updates that file, this names every
// colour that needs changing here
class UpstreamColorsTest {
  @Test
  fun `Built-in themes match upstream's resolved colours`() {
    val mismatches =
      upstream().mapNotNull { (theme, token, expected) ->
        val actual = Themes.getValue(theme).attributes()[token]?.toHex() ?: return@mapNotNull null
        if (actual == expected || theme to token in Allowed) {
          null
        } else {
          "$theme $token: aktual $actual, upstream $expected"
        }
      }
    assertThat(mismatches).isEmpty()
  }

  private fun upstream(): List<Triple<String, String, String>> =
    requireNotNull(javaClass.getResource("/last-known-resolved.txt")) {
        "last-known-resolved.txt isn't on the test classpath"
      }
      .readText()
      .lines()
      .filter { it.isNotBlank() }
      .map { line ->
        val (theme, token, value) = line.split(" ")
        Triple(theme, token, value)
      }

  private fun Color.toHex(): String {
    val argb = toArgb().toLong() and 0xFFFFFFFF
    val rgb = (argb and 0xFFFFFF).toString(16).padStart(6, '0')
    val alpha = argb ushr 24
    return if (alpha == 0xFFL) "#$rgb" else "#$rgb${alpha.toString(16).padStart(2, '0')}"
  }

  private companion object {
    val Themes = mapOf("light" to LightColors, "dark" to DarkColors, "midnight" to MidnightColors)

    // Colours aktual deliberately keeps different from upstream, as (theme, token)
    val Allowed = emptySet<Pair<String, String>>()
  }
}
