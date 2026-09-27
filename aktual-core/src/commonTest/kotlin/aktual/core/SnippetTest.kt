package aktual.core

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SnippetTest {
  @Test
  fun `Keeps text when the match is near the start`() {
    assertThat(snippet("Save for a holiday", "holiday")).isEqualTo("Save for a holiday")
  }

  @Test
  fun `Starts shortly before a later match, on a word boundary`() {
    val text = "This is quite a long piece of text that eventually mentions a holiday somewhere"
    assertThat(snippet(text, "HOLIDAY")).isEqualTo("…eventually mentions a holiday somewhere")
  }

  @Test
  fun `Uses a custom lead`() {
    val text = "This is quite a long piece of text that eventually mentions a holiday somewhere"
    assertThat(snippet(text, "holiday", lead = 5)).isEqualTo("…mentions a holiday somewhere")
  }

  @Test
  fun `Keeps text when there's no match`() {
    assertThat(snippet("Nothing to see", "holiday")).isEqualTo("Nothing to see")
  }
}
