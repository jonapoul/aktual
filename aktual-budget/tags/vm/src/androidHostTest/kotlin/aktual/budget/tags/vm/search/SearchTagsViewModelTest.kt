package aktual.budget.tags.vm.search

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.TagsDao
import aktual.budget.tags.vm.insertTag
import aktual.budget.tags.vm.list.TagItem
import aktual.budget.tags.vm.tombstoneTag
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import kotlin.test.AfterTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchTagsViewModelTest {
  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Blank query prompts for input`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    val viewModel = createViewModel(scope)

    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(NoQuery)
      viewModel.setQuery("   ")
      viewModel.setQuery("#")
      scope.advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `Matches names before descriptions, case-insensitively`() = runDatabaseTest { scope ->
    insertTag(id = "rent-id", tag = "rent", description = "Monthly FOOD and housing")
    insertTag(id = "groceries-id", tag = "groceries", description = "Weekly shop")
    insertTag(id = "food-id", tag = "Food")
    insertTag(id = "fuel-id", tag = "fuel")
    val viewModel = createViewModel(scope)

    viewModel.setQuery("food")

    viewModel.state.test {
      assertThat(awaitResults()).extracting(TagItem::tag).containsExactly("Food", "rent")
    }
  }

  @Test
  fun `Ignores a leading hash`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    val viewModel = createViewModel(scope)

    viewModel.setQuery(" #GROC ")

    viewModel.state.test {
      assertThat(awaitResults()).extracting(TagItem::tag).containsExactly("groceries")
    }
  }

  @Test
  fun `Trims the description to start near the match`() = runDatabaseTest { scope ->
    insertTag(
      id = "trips-id",
      tag = "trips",
      description = "Anything spent while away from home, including the summer holiday",
    )
    val viewModel = createViewModel(scope)

    viewModel.setQuery("holiday")

    viewModel.state.test {
      assertThat(awaitResults()).extracting(TagItem::description).containsExactly("…summer holiday")
    }
  }

  @Test
  fun `No matches shows no results`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    val viewModel = createViewModel(scope)

    viewModel.setQuery("xyz")

    viewModel.state.test {
      var state = awaitItem()
      while (state == NoQuery) state = awaitItem()
      assertThat(state).isEqualTo(NoResults)
    }
  }

  @Test
  fun `Skips deleted tags`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    insertTag(id = "gifts-id", tag = "gifts")
    tombstoneTag(id = "gifts-id")
    val viewModel = createViewModel(scope)

    viewModel.setQuery("g")

    viewModel.state.test {
      assertThat(awaitResults()).extracting(TagItem::tag).containsExactly("groceries")
    }
  }

  private suspend fun ReceiveTurbine<SearchTagsState>.awaitResults(): List<TagItem> {
    var state = awaitItem()
    while (state !is SearchTagsState.Results) state = awaitItem()
    cancelAndIgnoreRemainingEvents()
    return state.tags
  }

  private fun BudgetDatabase.createViewModel(scope: TestScope): SearchTagsViewModel {
    Dispatchers.setMain(scope.standardDispatcher)
    return SearchTagsViewModel(
      savedState = SavedStateHandle(),
      tagsDao = TagsDao(this, TestCoroutineContexts(scope.standardDispatcher)),
    )
  }
}
