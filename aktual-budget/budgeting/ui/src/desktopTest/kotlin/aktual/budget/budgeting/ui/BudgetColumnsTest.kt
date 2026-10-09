package aktual.budget.budgeting.ui

import aktual.budget.model.CategoryId
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.test.withKeyDown
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.datetime.YearMonth

@OptIn(ExperimentalTestApi::class)
class BudgetColumnsTest {
  private val actions = mutableListOf<BudgetAction>()
  private val september = YearMonth(2026, 9)
  private val october = YearMonth(2026, 10)

  @Test
  fun `Enter saves and moves down a row`() = runColumnsTest {
    // given
    cell(september, "Rent").performClick()

    // when
    editor().performTextReplacement("1000 + 50")
    editor().performKeyInput { pressKey(Enter) }

    // then
    assertThat(actions).containsExactly(SetBudget(september, CategoryId("Rent"), "1000 + 50"))
    cell(september, "Utilities").assertIsSelected()
    cell(september, "Rent").assertIsNotSelected()
  }

  @Test
  fun `Shift and Tab saves and moves up a row`() = runColumnsTest {
    // given
    cell(september, "Utilities").performClick()

    // when
    editor().performTextReplacement("80")
    editor().performKeyInput { withKeyDown(ShiftLeft) { pressKey(Tab) } }

    // then
    assertThat(actions).containsExactly(SetBudget(september, CategoryId("Utilities"), "80"))
    cell(september, "Rent").assertIsSelected()
  }

  @Test
  fun `Escape cancels the edit`() = runColumnsTest {
    // given
    cell(september, "Rent").performClick()

    // when
    editor().performTextReplacement("1")
    editor().performKeyInput { pressKey(Escape) }

    // then
    assertThat(actions).isEmpty()
    cell(september, "Rent").assertIsSelected()
  }

  @Test
  fun `Arrow keys move between cells while not editing`() = runColumnsTest {
    // given
    cell(september, "Rent").performClick()
    editor().performKeyInput { pressKey(Escape) }

    // when
    table().performKeyInput {
      pressKey(DirectionDown)
      pressKey(DirectionRight)
    }

    // then
    cell(october, "Utilities").assertIsSelected()
    assertThat(actions).isEmpty()
  }

  @Test
  fun `Moving down skips collapsed groups`() = runColumnsTest {
    // given Parking is the last row before the collapsed Savings group, then income
    cell(september, "Parking").performClick()

    // when
    editor().performKeyInput { pressKey(Enter) }

    // then envelope income can't be budgeted, so the cursor stays put
    cell(september, "Parking").assertIsSelected()
    assertThat(actions).isEmpty()
  }

  @Test
  fun `Balance menu toggles rollover`() = runColumnsTest {
    // when
    onAllNodes(hasClickLabel("Balance options for Utilities"))[0].performClick()
    onNodeWithText("Rollover overspending").performClick()

    // then
    assertThat(actions).containsExactly(ToggleCarryover(september, CategoryId("Utilities"), true))
  }

  @Test
  fun `Summary card opens the month summary`() = runColumnsTest {
    // when
    onNode(hasClickLabel("Budget summary for October 2026")).performClick()

    // then
    assertThat(actions).containsExactly(OpenSheet(SheetRequest.SummarySheet(october)))
  }

  private fun hasClickLabel(label: String) =
    SemanticsMatcher("click label $label") {
      it.config.getOrNull(SemanticsActions.OnClick)?.label == label
    }

  private fun ComposeUiTest.cell(month: YearMonth, category: String): SemanticsNodeInteraction =
    onNodeWithTag(BudgetCell(month, CategoryId(category)).tag, useUnmergedTree = true)

  private fun ComposeUiTest.editor(): SemanticsNodeInteraction = onNode(hasSetTextAction())

  private fun ComposeUiTest.table(): SemanticsNodeInteraction = onNodeWithTag(BudgetTags.Columns)

  private fun runColumnsTest(test: ComposeUiTest.() -> Unit) = runComposeUiTest {
    setContent {
      PreviewWithColors(DarkColors) {
        BudgetColumns(
          state = previewColumns(count = 2),
          listState = rememberLazyListState(),
          onAction = { actions += it },
        )
      }
    }
    test()
  }
}
