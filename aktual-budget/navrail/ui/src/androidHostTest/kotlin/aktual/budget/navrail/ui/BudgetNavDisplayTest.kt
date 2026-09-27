package aktual.budget.navrail.ui

import aktual.budget.model.DashboardPageId
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateReportNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.NavStackImpl
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.budgetEntry
import aktual.test.runTest
import aktual.test.setAndroidThemedContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation3.runtime.EntryProviderScope
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BudgetNavDisplayTest {
  @get:Rule val composeRule = createComposeRule()

  @Test
  fun `Scroll position survives pushing and popping an entry in the same tab`() =
    composeRule.runTest {
      // given
      val stack =
        NavStackImpl<BudgetNavKey>(
          appCloser = null,
          stack = mutableStateListOf(ReportsListNavRoute),
        )
      setAndroidThemedContent {
        BudgetNavDisplay(contributors = persistentSetOf(TestContributor()), activeStack = stack)
      }

      // when the list is scrolled down
      onNodeWithTag(LIST_TAG).performScrollToIndex(SCROLLED_INDEX)
      onNodeWithText(SCROLLED_ITEM).assertIsDisplayed()

      // and another entry in the same tab is opened, then closed
      runOnIdle { stack.push(CreateReportNavRoute(DashboardPageId("page"))) }
      onNodeWithText(OTHER_SCREEN).assertIsDisplayed()
      runOnIdle { stack.pop() }

      // then the list is still scrolled down
      onNodeWithText(SCROLLED_ITEM).assertIsDisplayed()
    }

  private class TestContributor : BudgetNavEntryContributor {
    override fun EntryProviderScope<BudgetNavKey>.contribute(stack: NavStack<BudgetNavKey>) {
      budgetEntry<ReportsListNavRoute> {
        LazyColumn(modifier = Modifier.testTag(LIST_TAG), state = rememberLazyListState()) {
          items(count = 100) { i -> Text("Item $i") }
        }
      }

      budgetEntry<CreateReportNavRoute> { Text(OTHER_SCREEN) }
    }
  }

  private companion object {
    const val LIST_TAG = "list"
    const val OTHER_SCREEN = "Other screen"
    const val SCROLLED_INDEX = 80
    const val SCROLLED_ITEM = "Item $SCROLLED_INDEX"
  }
}
