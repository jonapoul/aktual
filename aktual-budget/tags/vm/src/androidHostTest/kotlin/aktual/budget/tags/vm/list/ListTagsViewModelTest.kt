package aktual.budget.tags.vm.list

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.TagId
import aktual.budget.tags.vm.insertTag
import aktual.test.TestSyncController
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

      // then only the matching tag remains - drain past the intermediate emission produced
      // when search opens but the filter text hasn't been set yet
      var success = awaitItem() as Success
      while (success.filterText != "food") {
        success = awaitItem() as Success
      }
      assertThat(success).prop(Success::tags).extracting(TagItem::tag).containsExactly("groceries")

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Delete tombstones the tag, removes it, and emits an event`() = runDatabaseTest {
    insertTag(id = "groceries-id", tag = "groceries")
    insertTag(id = "rent-id", tag = "rent")

    val sync = TestSyncController()
    val viewModel = createViewModel(sync)

    // wait for the initial load with both tags
    viewModel.state.test {
      assertThat(awaitItem()).isInstanceOf(Success::class).prop(Success::tags).hasSize(2)
      cancelAndIgnoreRemainingEvents()
    }

    // deleting one tag emits a confirmation event carrying the removed item
    viewModel.events.test {
      viewModel.delete(TagId("groceries-id"))
      val event = assertThat(awaitItem()).isInstanceOf(ListTagsEvent.Deleted::class)
      event.prop(ListTagsEvent.Deleted::item).prop(TagItem::tag).isEqualTo("groceries")
    }

    // a tombstone change was queued for sync
    assertThat(sync.changes)
      .extracting(LocalChange::row, LocalChange::column)
      .containsExactly("groceries-id" to "tombstone")

    // and only the remaining tag is shown
    viewModel.state.test {
      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::tags)
        .extracting(TagItem::tag)
        .containsExactly("rent")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Undo restores a deleted tag at its original position`() = runDatabaseTest {
    insertTag(id = "groceries-id", tag = "groceries")
    insertTag(id = "rent-id", tag = "rent")

    val sync = TestSyncController()
    val viewModel = createViewModel(sync)

    // wait for the initial load with both tags
    viewModel.state.test {
      assertThat(awaitItem()).isInstanceOf(Success::class).prop(Success::tags).hasSize(2)
      cancelAndIgnoreRemainingEvents()
    }

    // delete the first tag, capturing the event we'd hand back to undo
    lateinit var deleted: ListTagsEvent.Deleted
    viewModel.events.test {
      viewModel.delete(TagId("groceries-id"))
      val event = awaitItem()
      assertThat(event).isInstanceOf(ListTagsEvent.Deleted::class)
      deleted = event as ListTagsEvent.Deleted
    }

    // undoing puts it back where it was
    viewModel.undoDelete(deleted.item, deleted.index)
    viewModel.state.test {
      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::tags)
        .extracting(TagItem::tag)
        .containsExactly("groceries", "rent")
      cancelAndIgnoreRemainingEvents()
    }

    // the delete tombstone and its reversal were both queued for sync
    assertThat(sync.changes)
      .extracting(LocalChange::row, LocalChange::column, LocalChange::value)
      .containsExactly(
        Triple("groceries-id", "tombstone", MessageValue.Number(1)),
        Triple("groceries-id", "tombstone", MessageValue.Number(0)),
      )
  }

  @Test
  fun `Delete failure emits a DeleteFailed event and keeps the tag`() = runDatabaseTest {
    insertTag(id = "groceries-id", tag = "groceries")

    val viewModel = createViewModel(sync = FailingSyncController())

    // wait for the initial load
    viewModel.state.test {
      assertThat(awaitItem()).isInstanceOf(Success::class).prop(Success::tags).hasSize(1)
      cancelAndIgnoreRemainingEvents()
    }

    // a failing sync surfaces a DeleteFailed event rather than failing silently
    viewModel.events.test {
      viewModel.delete(TagId("groceries-id"))
      assertThat(awaitItem()).isEqualTo(ListTagsEvent.DeleteFailed("groceries"))
    }

    // and the tag is still present since the delete didn't go through
    viewModel.state.test {
      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::tags)
        .extracting(TagItem::tag)
        .containsExactly("groceries")
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Undo failure emits a RestoreFailed event and doesn't re-insert the tag`() = runDatabaseTest {
    insertTag(id = "rent-id", tag = "rent")

    val viewModel = createViewModel(sync = FailingSyncController())

    // wait for the initial load
    viewModel.state.test {
      assertThat(awaitItem()).isInstanceOf(Success::class).prop(Success::tags).hasSize(1)
      cancelAndIgnoreRemainingEvents()
    }

    // a failing sync while undoing surfaces a RestoreFailed event rather than failing silently
    val deleted =
      TagItem(
        id = TagId("groceries-id"),
        tag = "groceries",
        color = null,
        description = "",
        hidden = false,
      )
    viewModel.events.test {
      viewModel.undoDelete(deleted, index = 0)
      assertThat(awaitItem()).isEqualTo(ListTagsEvent.RestoreFailed("groceries"))
    }

    // and the tag wasn't re-inserted since the restore didn't go through
    viewModel.state.test {
      assertThat(awaitItem())
        .isInstanceOf(Success::class)
        .prop(Success::tags)
        .extracting(TagItem::tag)
        .containsExactly("rent")
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun BudgetDatabase.createViewModel(sync: BudgetSyncController = TestSyncController()) =
    ListTagsViewModel(SavedStateHandle(), TagsDao(this), sync)

  private class FailingSyncController : BudgetSyncController {
    override suspend fun syncChanges(changes: List<LocalChange>): Unit = error("sync failed")

    override fun schedule() = Unit
  }
}
