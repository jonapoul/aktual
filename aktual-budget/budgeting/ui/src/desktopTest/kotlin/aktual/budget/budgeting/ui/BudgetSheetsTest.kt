package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetState
import aktual.budget.budgeting.vm.BudgetSummary
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.core.theme.DarkColors
import aktual.core.ui.PreviewWithColors
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.YearMonth

@OptIn(ExperimentalTestApi::class)
class BudgetSheetsTest {
  private val actions = mutableListOf<BudgetAction>()
  private val october = YearMonth(2026, 10)
  private val eatingOut = CategoryId("Eating out")
  private val utilities = CategoryId("Utilities")

  @Test
  fun `Cover sources are To Budget and categories with money left`() =
    runSheetTest(
      SheetRequest.CoverSheet(october, eatingOut),
    ) {
      onNodeWithText("To budget").assertExists()
      onNodeWithText("Utilities").assertExists()
      onNodeWithText("Rent").assertDoesNotExist()
    }

  @Test
  fun `Cover sources leave out To Budget when overbudgeted`() =
    runSheetTest(
      SheetRequest.CoverSheet(october, eatingOut),
      state = overbudgeted(),
    ) {
      onNodeWithText("To budget").assertDoesNotExist()
      onNodeWithText("Utilities").assertExists()
    }

  @Test
  fun `Transfers need a positive amount`() =
    runSheetTest(
      SheetRequest.TransferSheet(october, utilities),
    ) {
      // given
      amount().performTextReplacement("0")
      confirm().assertIsNotEnabled()
      amount().performTextReplacement("abc")
      confirm().assertIsNotEnabled()

      // when
      amount().performTextReplacement("15")
      // A pointer click doesn't reach the sheet's buttons in desktop tests
      confirm().assertIsEnabled().performSemanticsAction(SemanticsActions.OnClick)
      waitForIdle()

      // then To Budget is picked first
      assertThat(actions).containsExactly(TransferBudget(october, "15", utilities, to = null))
    }

  private fun ComposeUiTest.amount() = onNode(hasSetTextAction())

  private fun ComposeUiTest.confirm() = onNodeWithText("Transfer")

  private fun overbudgeted(): BudgetState.Loaded {
    val state = previewColumns(count = 2)
    return state.copy(
      months =
        state.months
          .map { month ->
            val summary = month.summary as BudgetSummary.Envelope
            month.copy(summary = summary.copy(toBudget = Amount(-10.0)))
          }
          .toImmutableList(),
    )
  }

  private fun runSheetTest(
    request: SheetRequest,
    state: BudgetState.Loaded = previewColumns(count = 2),
    test: ComposeUiTest.() -> Unit,
  ) = runComposeUiTest {
    setContent {
      PreviewWithColors(DarkColors) {
        BudgetSheets(request = request, state = state, onAction = { actions += it }, onDismiss = {})
      }
    }
    test()
  }
}
