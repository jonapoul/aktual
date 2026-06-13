package aktual.budget.tags.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.TagId
import aktual.test.runDatabaseTest
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.extracting
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ListTagsViewModelTest {
  @Test
  fun `Loads and maps tags from the database`() = runDatabaseTest {
    // given a couple of tags - one with an explicit hex color, one with neither color nor
    // description
    insertTag(
      id = "groceries-id",
      tag = "groceries",
      color = "#aabbcc",
      description = "Weekly food shopping",
    )
    insertTag(id = "rent-id", tag = "rent")

    // when the VM loads
    val viewModel = createViewModel()

    // then the state settles on the mapped tags
    viewModel.state.test {
      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::tags)
        .containsExactlyInAnyOrder(
          TagItem(
            id = TagId("groceries-id"),
            tag = "groceries",
            color = Color(0xFFAABBCC),
            description = "Weekly food shopping",
            hidden = false,
          ),
          TagItem(
            id = TagId("rent-id"),
            tag = "rent",
            color = null,
            description = "",
            hidden = false,
          ),
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Empty database yields the empty state`() = runDatabaseTest {
    // when the VM loads with no tags in the database
    val viewModel = createViewModel()

    // then we get the empty state rather than a failure
    viewModel.state.test {
      assertThat(awaitItem()).isEqualTo(Empty)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Filter matches against tag name and description`() = runDatabaseTest {
    insertTag(id = "groceries-id", tag = "groceries", description = "Weekly food shopping")
    insertTag(id = "rent-id", tag = "rent", description = "Monthly housing")

    val viewModel = createViewModel()

    viewModel.state.test {
      // wait for the initial unfiltered load
      assertThat(awaitItem()).isInstanceOf(Success::class).prop(Success::tags).hasSize(2)

      // when a search filter is applied that only matches one tag's description
      viewModel.openSearch()
      viewModel.setFilterText("food")

      // then only the matching tag remains
      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::tags)
        .extracting(TagItem::tag)
        .containsExactly("groceries")

      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun BudgetDatabase.createViewModel() =
    ListTagsViewModel(SavedStateHandle(), TagsDao(this))

  private suspend fun BudgetDatabase.insertTag(
    id: String,
    tag: String,
    color: String? = null,
    description: String? = null,
  ) = tagsQueries.insert(id = TagId(id), tag = tag, color = color, description = description)
}
