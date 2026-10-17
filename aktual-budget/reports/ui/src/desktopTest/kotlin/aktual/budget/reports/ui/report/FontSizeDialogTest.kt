package aktual.budget.reports.ui.report

import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.FontSizeMode
import aktual.test.setPreviewContent
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class FontSizeDialogTest {
  private var applied: Pair<FontSizeMode, Double>? = null

  @Test
  fun `Applies a typed static size`() = runComposeUiTest {
    // given
    setPreviewContent {
      FontSizeDialog(
        initialMode = Dynamic,
        initialSize = 32.0,
        preview = PREVIEW_FORMULA_DATA,
        onApply = { mode, size -> applied = mode to size },
        onDismiss = {},
      )
    }

    // when
    onNodeWithText("Static").performClick()
    onNodeWithTag(Tags.FontSizeField).performTextReplacement("48")
    onNodeWithTag(Tags.ApplyFontSize).performClick()

    // then
    assertThat(applied).isEqualTo(FontSizeMode.Static to 48.0)
  }

  @Test
  fun `Ignores a typed size out of range`() = runComposeUiTest {
    // given
    setPreviewContent {
      FontSizeDialog(
        initialMode = Static,
        initialSize = 32.0,
        preview = PREVIEW_FORMULA_DATA,
        onApply = { mode, size -> applied = mode to size },
        onDismiss = {},
      )
    }

    // when
    onNodeWithTag(Tags.FontSizeField).performTextReplacement("500")
    onNodeWithTag(Tags.ApplyFontSize).performClick()

    // then
    assertThat(applied).isEqualTo(FontSizeMode.Static to 32.0)
  }
}
