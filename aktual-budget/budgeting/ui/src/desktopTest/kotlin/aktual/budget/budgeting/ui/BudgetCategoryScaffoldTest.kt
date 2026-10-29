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
      onNodeWithText("Food · Hidden").assertExists()
      onNodeWithTag(BudgetTags.CategoryMenu).performClick()
      onNodeWithText("Unhide category").performClick()

      assertThat(hidden).containsExactly(false)
    }

  @Test
  fun `No menu when the group is hidden`() =
    runCategoryTest(PREVIEW_CATEGORY.copy(isHidden = true, isGroupHidden = true)) {
      onNodeWithTag(BudgetTags.CategoryMenu).assertDoesNotExist()
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
        )
      }
      test()
    }
}
