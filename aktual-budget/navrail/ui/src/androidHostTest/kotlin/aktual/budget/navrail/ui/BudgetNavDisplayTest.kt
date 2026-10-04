package aktual.budget.navrail.ui

import aktual.budget.model.AccountId
import aktual.budget.model.DashboardPageId
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.CreateReportNavRoute
import aktual.core.nav.HomeNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.NavStackImpl
import aktual.core.nav.ReportsListNavRoute
import aktual.test.runTest
import aktual.test.setAndroidThemedContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.navigation3.runtime.NavKey
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
        BudgetNavDisplay(
          contributors = persistentSetOf(TestContributor()),
          appStack = NavStackImpl(appCloser = null, stack = mutableStateListOf()),
          activeStack = stack,
          selectedTab = Reports,
        )
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

  @Test
  fun `Route from another tab can be pushed onto a tab's stack and popped`() = composeRule.runTest {
    // given
    val stack =
      NavStackImpl<BudgetNavKey>(appCloser = null, stack = mutableStateListOf(HomeNavRoute))
    setAndroidThemedContent {
      BudgetNavDisplay(
        contributors = persistentSetOf(TestContributor()),
        appStack = NavStackImpl(appCloser = null, stack = remember { mutableStateListOf() }),
        activeStack = stack,
        selectedTab = Home,
      )
    }
    onNodeWithText(HOME_SCREEN).assertIsDisplayed()

    // when a transactions route is pushed onto the home stack
    runOnIdle { stack.push(AccountTransactionsNavRoute(AccountId("account"))) }

    // then it's shown
    onNodeWithText(ACCOUNT_SCREEN).assertIsDisplayed()

    // and back returns to home
    runOnIdle { stack.pop() }
    onNodeWithText(HOME_SCREEN).assertIsDisplayed()
  }

  private class TestContributor : BudgetNavEntryContributor {
    override fun BudgetEntryScope.contribute(
      stack: NavStack<BudgetNavKey>,
      appStack: NavStack<NavKey>,
    ) {
      budgetEntry<ReportsListNavRoute> {
        LazyColumn(modifier = Modifier.testTag(LIST_TAG), state = rememberLazyListState()) {
          items(count = 100) { i -> Text("Item $i") }
        }
      }

      budgetEntry<CreateReportNavRoute> { Text(OTHER_SCREEN) }

      budgetEntry<HomeNavRoute> { Text(HOME_SCREEN) }

      budgetEntry<AccountTransactionsNavRoute> { Text(ACCOUNT_SCREEN) }
    }
  }

  private companion object {
    const val LIST_TAG = "list"
    const val OTHER_SCREEN = "Other screen"
    const val HOME_SCREEN = "Home screen"
    const val ACCOUNT_SCREEN = "Account screen"
    const val SCROLLED_INDEX = 80
    const val SCROLLED_ITEM = "Item $SCROLLED_INDEX"
  }
}
