package aktual.budget.reports.ui.report

import aktual.budget.formula.ErrorValue
import aktual.budget.reports.ui.Tags
import aktual.budget.reports.ui.charts.PREVIEW_FORMULA_DATA
import aktual.budget.reports.vm.FormulaData
import aktual.budget.reports.vm.FormulaOutput
import aktual.test.setPreviewContent
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf

class FormulaFieldScreenTest {
  private val changes = mutableListOf<String>()
  private var closed = false

  @Test
  fun `Typing sends each change and Done keeps it`() = runComposeUiTest {
    // given
    setScreen()

    // when
    onNodeWithTag(Tags.FormulaField).performTextReplacement("=1 + 1")
    onNodeWithTag(Tags.FormulaDone).performClick()

    // then
    waitForIdle()
    assertThat(changes.last()).isEqualTo("=1 + 1")
    assertThat(closed).isTrue()
  }

  @Test
  fun `Back puts the original text back`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.FormulaField).performTextReplacement("=1 + 1")
    waitForIdle()

    // when
    onNodeWithContentDescription("Navigate back").performClick()

    // then
    assertThat(changes.last()).isEqualTo(INITIAL)
    assertThat(closed).isTrue()
  }

  @Test
  fun `Errors replace the result`() = runComposeUiTest {
    // when
    setScreen(
      PREVIEW_FORMULA_DATA.copy(
        output = FormulaOutput.Failure(ErrorValue(Generic, UnexpectedEnd, null)),
      ),
    )

    // then
    onNodeWithTag(Tags.FormulaResult).assertDoesNotExist()
    onNodeWithText("#ERROR!\nThe formula ends too soon").assertExists()
  }

  @Test
  fun `Shortcuts insert at the cursor`() = runComposeUiTest {
    // given
    setScreen()
    onNodeWithTag(Tags.FormulaField).performTextReplacement("=")

    // when
    onNodeWithText("SUM()").performScrollTo().performClick()
    onNodeWithText("\"groceries\"").performScrollTo().performClick()

    // then
    waitForIdle()
    assertThat(changes.last()).isEqualTo("=SUM(\"groceries\")")
  }

  private fun ComposeUiTest.setScreen(preview: FormulaData = PREVIEW_FORMULA_DATA) =
    setPreviewContent {
      FormulaFieldScreen(
        field = Formula,
        initialText = INITIAL,
        preview = preview,
        queryNames = persistentListOf("groceries"),
        onChange = { changes += it },
        onClose = { closed = true },
      )
    }

  private companion object {
    const val INITIAL = "=SUM(1, 2)"
  }
}
