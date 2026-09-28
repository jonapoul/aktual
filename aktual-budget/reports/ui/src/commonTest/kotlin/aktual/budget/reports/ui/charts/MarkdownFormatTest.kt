package aktual.budget.reports.ui.charts

import androidx.compose.ui.text.TextRange
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class MarkdownFormatTest {
  @Test
  fun `Bold wraps the selection and keeps it selected`() {
    assertThat(format("a word here", TextRange(2, 6), Bold))
      .isEqualTo(FormattedText("a **word** here", TextRange(4, 8)))
  }

  @Test
  fun `Italic with no selection puts the cursor between the markers`() {
    assertThat(format("ab", TextRange(1), Italic)).isEqualTo(FormattedText("a__b", TextRange(2)))
  }

  @Test
  fun `Heading is added to the start of the current line`() {
    assertThat(format("one\ntwo", TextRange(5), Heading))
      .isEqualTo(FormattedText("one\n## two", TextRange(8)))
  }

  @Test
  fun `Heading is removed if the line already has one`() {
    assertThat(format("one\n## two", TextRange(8), Heading))
      .isEqualTo(FormattedText("one\ntwo", TextRange(5)))
  }

  @Test
  fun `Removing a prefix with the cursor inside it moves the cursor to the line start`() {
    assertThat(format("- item", TextRange(1), Bullet))
      .isEqualTo(FormattedText("item", TextRange(0)))
  }

  @Test
  fun `Bullet on the first line`() {
    assertThat(format("item", TextRange(0), Bullet))
      .isEqualTo(FormattedText("- item", TextRange(2)))
  }

  @Test
  fun `Link with a selection uses it as the label and puts the cursor in the url`() {
    assertThat(format("see docs", TextRange(4, 8), Link))
      .isEqualTo(FormattedText("see [docs]()", TextRange(11)))
  }

  @Test
  fun `Link with no selection puts the cursor in the label`() {
    assertThat(format("see ", TextRange(4), Link))
      .isEqualTo(FormattedText("see []()", TextRange(5)))
  }
}
