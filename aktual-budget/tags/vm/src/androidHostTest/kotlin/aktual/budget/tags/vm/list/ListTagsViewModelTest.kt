package aktual.budget.tags.vm.list

import aktual.budget.db.dao.TagsDao
import aktual.budget.model.TagId
import aktual.test.runDatabaseTest
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isInstanceOf
import assertk.assertions.prop
import kotlinx.coroutines.cancel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ListTagsViewModelTest {
  @Test
  fun `Loads and maps tags from the database`() = runDatabaseTest {
    // given a couple of tags - one with an explicit hex color, one with neither color nor
    // description
    tagsQueries.insert(
      id = TagId("groceries-id"),
      tag = "groceries",
      color = "#aabbcc",
      description = "Weekly food shopping",
    )
    tagsQueries.insert(
      id = TagId("rent-id"),
      tag = "rent",
      color = null,
      description = null,
    )

    // when the VM loads
    val viewModel = ListTagsViewModel(TagsDao(this))

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

    viewModel.viewModelScope.cancel()
  }

  @Test
  fun `Empty database yields an empty success`() = runDatabaseTest {
    // when the VM loads with no tags in the database
    val viewModel = ListTagsViewModel(TagsDao(this))

    // then we get an empty success rather than a failure
    viewModel.state.test {
      assertThat(awaitItem()).isInstanceOf(Success::class).prop(Success::tags).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }

    viewModel.viewModelScope.cancel()
  }
}
