package aktual.budget.budgeting.ui

import aktual.budget.model.CategoryId
import aktual.core.ui.Tags
import aktual.test.setPreviewContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlin.test.Test
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth

class BudgetSheetStackTest {
  private val actions = mutableListOf<BudgetAction>()
  private val october = YearMonth(2026, 10)
  private val utilities = CategoryId("Utilities")
  private val balance = SheetRequest.BalanceSheet(october, utilities)
  private val transfer = SheetRequest.TransferSheet(october, utilities)
  private var sheets by mutableStateOf<PersistentList<SheetRequest>>(persistentListOf(balance))

  @Test
  fun `Opening a sheet from the balance menu stacks it, and back returns to the menu`() =
    runStackTest {
      // given
      back().assertDoesNotExist()

      // when
      click(TRANSFER_ITEM)

      // then
      assertThat(sheets).containsExactly(balance, transfer)
      onNodeWithText(TRANSFER_TITLE).assertExists()

      // when
      back().performSemanticsAction(SemanticsActions.OnClick)
      waitForIdle()

      // then
      assertThat(sheets).containsExactly(balance)
      onNodeWithText(MENU_TITLE).assertExists()
      back().assertDoesNotExist()
    }

  @Test
  fun `Cancelling a stacked sheet returns to the menu`() = runStackTest {
    // given
    click(TRANSFER_ITEM)

    // when
    click("Cancel")

    // then
    assertThat(sheets).containsExactly(balance)
    onNodeWithText(MENU_TITLE).assertExists()
    assertThat(actions).isEmpty()
  }

  @Test
  fun `Confirming a stacked sheet closes the whole stack`() = runStackTest {
    // given
    click(TRANSFER_ITEM)
    onNode(hasSetTextAction()).performTextReplacement("15")

    // when
    click("Transfer")

    // then
    assertThat(sheets).isEmpty()
    assertThat(actions).containsExactly(TransferBudget(october, "15", utilities, to = null))
  }

  private fun runStackTest(test: ComposeUiTest.() -> Unit) = runComposeUiTest {
    val state = previewColumns(count = 2)
    setPreviewContent {
      BudgetSheetStack(
        sheets = sheets,
        state = state,
        onAction = { actions += it },
        onOpen = { sheet -> sheets = sheets.adding(sheet) },
        onChange = { sheets = it },
      )
    }
    onNodeWithText(MENU_TITLE).assertExists()
    test()
  }

  // A pointer click doesn't reach the sheet's buttons in desktop tests
  private fun ComposeUiTest.click(text: String) {
    onNodeWithText(text).performSemanticsAction(SemanticsActions.OnClick)
    waitForIdle()
  }

  private fun ComposeUiTest.back() = onNodeWithTag(Tags.SheetBack)

  private companion object {
    const val MENU_TITLE = "Utilities · October 2026"
    const val TRANSFER_ITEM = "Transfer to another category"
    const val TRANSFER_TITLE = "Transfer from Utilities"
  }
}
