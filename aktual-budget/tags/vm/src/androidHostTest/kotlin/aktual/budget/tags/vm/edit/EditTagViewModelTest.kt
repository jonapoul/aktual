package aktual.budget.tags.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetTag
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.TagId
import aktual.budget.tags.vm.insertTag
import aktual.core.model.UuidGenerator
import aktual.test.runDatabaseTest
import androidx.compose.ui.graphics.Color
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditTagViewModelTest {
  @Test
  fun `New tag starts as an empty editing state`() = runDatabaseTest {
    val viewModel = createViewModel(id = null)

    viewModel.state.test {
      assertThat(awaitEditing()).isEqualTo(EditTagState.Editing(isNew = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Loads an existing tag from the database`() = runDatabaseTest {
    insertTag(
      id = "groceries-id",
      tag = "groceries",
      color = "#aabbcc",
      description = "Weekly food shopping",
    )

    val viewModel = createViewModel(id = TagId("groceries-id"))

    viewModel.state.test {
      assertThat(awaitEditing())
        .isEqualTo(
          EditTagState.Editing(
            initialTag = "groceries",
            initialDescription = "Weekly food shopping",
            color = Color(0xFFAABBCC),
            isNew = false,
          )
        )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Requesting a tag that doesn't exist surfaces a failure`() = runDatabaseTest {
    val viewModel = createViewModel(id = TagId("missing-id"))

    viewModel.state.test {
      assertThat(awaitFailure()).isEqualTo(EditTagState.Failure(cause = null))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Tracks unsaved changes against the loaded values`() = runDatabaseTest {
    insertTag(id = "groceries-id", tag = "groceries")
    val viewModel = createViewModel(id = TagId("groceries-id"))
    viewModel.awaitLoaded()

    viewModel.hasUnsavedChanges.test {
      assertThat(awaitItem()).isFalse()

      viewModel.setTag("groceries-changed")
      assertThat(awaitItem()).isTrue()

      // editing back to the original value clears the unsaved-changes flag
      viewModel.setTag("groceries")
      assertThat(awaitItem()).isFalse()
    }
  }

  @Test
  fun `Cannot save a blank tag or one with an invalid colour`() = runDatabaseTest {
    val viewModel = createViewModel(id = null)
    viewModel.awaitLoaded()

    viewModel.canSave.test {
      assertThat(awaitItem()).isFalse()

      viewModel.setTag("groceries")
      assertThat(awaitItem()).isTrue()

      viewModel.setColorError(isError = true)
      assertThat(awaitItem()).isFalse()
    }
  }

  @Test
  fun `Saving a new tag persists it, syncs the change and emits the saved event`() =
    runDatabaseTest {
      val sync = RecordingSyncController()
      val viewModel = createViewModel(id = null, sync = sync)
      viewModel.awaitLoaded()

      viewModel.setTag("groceries")
      viewModel.setDescription("Weekly food shopping")
      viewModel.setColor(Color(0xFFAABBCC))

      viewModel.events.test {
        viewModel.save()
        assertThat(awaitItem()).isEqualTo(EditTagEvent.FinishedSaving)
      }

      val saved = TagsDao(this).getTag(TagId(GENERATED_ID))
      assertThat(saved).isNotNull().all {
        prop(GetTag::tag).isEqualTo("groceries")
        prop(GetTag::color).isEqualTo("#aabbcc")
        prop(GetTag::description).isEqualTo("Weekly food shopping")
      }

      assertThat(sync.changes).isNotEmpty()
    }

  private suspend fun EditTagViewModel.awaitLoaded() {
    state.test {
      awaitEditing()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private suspend fun ReceiveTurbine<EditTagState>.awaitEditing(): EditTagState.Editing {
    while (true) {
      val item = awaitItem()
      if (item is EditTagState.Editing) return item
    }
  }

  private suspend fun ReceiveTurbine<EditTagState>.awaitFailure(): EditTagState.Failure {
    while (true) {
      val item = awaitItem()
      if (item is EditTagState.Failure) return item
    }
  }

  private fun BudgetDatabase.createViewModel(
    id: TagId?,
    uuid: UuidGenerator = UuidGenerator { GENERATED_ID },
    sync: BudgetSyncController = RecordingSyncController(),
  ) =
    EditTagViewModel(
      tagId = id,
      tagsDao = TagsDao(this),
      uuidGenerator = uuid,
      syncController = sync,
    )

  private class RecordingSyncController : BudgetSyncController {
    val changes = mutableListOf<LocalChange>()

    override suspend fun syncChanges(vararg changes: LocalChange) {
      this.changes += changes
    }

    override suspend fun syncChanges(changes: List<LocalChange>) {
      this.changes += changes
    }

    override fun schedule() = Unit
  }

  private companion object {
    const val GENERATED_ID = "generated-id"
  }
}
