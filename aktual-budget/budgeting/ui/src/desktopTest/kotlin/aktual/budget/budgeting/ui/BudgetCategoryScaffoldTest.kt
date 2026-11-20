package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.BudgetCategoryState
import aktual.test.setPreviewContent
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BudgetCategoryScaffoldTest {
  private val hidden = mutableListOf<Boolean>()
  private val edits = mutableListOf<String>()

  @Test
  fun `Hides the category from its menu`() =
    runCategoryTest(PREVIEW_CATEGORY) {
      onNodeWithTag(BudgetTags.CategoryMenu).performClick()
      onNodeWithText("Hide category").performClick()

      assertThat(hidden).containsExactly(true)
    }

  @Test
  fun `Unhides a hidden category from its menu`() =
    runCategoryTest(PREVIEW_CATEGORY.copy(isHidden = true)) {
      onNodeWithText("Food, Hidden").assertExists()
      onNodeWithTag(BudgetTags.CategoryMenu).performClick()
      onNodeWithText("Unhide category").performClick()

      assertThat(hidden).containsExactly(false)
    }

  @Test
  fun `No hide option when the group is hidden`() =
    runCategoryTest(PREVIEW_CATEGORY.copy(isHidden = true, isGroupHidden = true)) {
      onNodeWithTag(BudgetTags.CategoryMenu).performClick()
      onNodeWithText("Unhide category").assertDoesNotExist()
      onNodeWithText("Rename category").assertExists()
    }

  @Test
  fun `Renames and deletes from the menu`() =
    runCategoryTest(PREVIEW_CATEGORY) {
      onNodeWithTag(BudgetTags.CategoryMenu).performClick()
      onNodeWithText("Rename category").performClick()
      onNodeWithTag(BudgetTags.CategoryMenu).performClick()
      onNodeWithText("Delete category").performClick()

      assertThat(edits).containsExactly("rename Groceries", "delete Groceries")
    }

  private fun runCategoryTest(state: BudgetCategoryState, test: ComposeUiTest.() -> Unit) =
    runComposeUiTest {
      setPreviewContent {
        BudgetCategoryScaffold(
          state = state,
          onBack = {},
          onSelect = {},
          onViewTransactions = {},
          onHide = { hidden += it },
          onRename = { edits += "rename $it" },
          onDelete = { edits += "delete $it" },
        )
      }
      test()
    }
}
