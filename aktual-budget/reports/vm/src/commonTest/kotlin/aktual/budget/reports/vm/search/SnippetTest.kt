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
}
