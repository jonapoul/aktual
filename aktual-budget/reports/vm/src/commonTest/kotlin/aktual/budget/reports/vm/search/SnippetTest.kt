package aktual.budget.reports.vm.search

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class SnippetTest {
  @Test
  fun `Strips markdown`() {
    val markdown = "# Title\n\nSave **more** for [holidays](https://example.com) and `stuff`"
    assertThat(markdown.markdownToPlainText()).isEqualTo("Title Save more for holidays and stuff")
  }

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
  fun `Keeps text when there's no match`() {
    assertThat(snippet("Nothing to see", "holiday")).isEqualTo("Nothing to see")
  }
}
