package aktual.budget.tags.vm.edit

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetTag
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.TagId
import aktual.budget.tags.vm.insertTag
import aktual.budget.tags.vm.tombstoneTag
import aktual.core.model.UuidGenerator
import aktual.prefs.TagPreferences
import aktual.prefs.TagPreferencesImpl
import aktual.test.TestSyncController
import aktual.test.assertThatNextEmission
import aktual.test.buildPreferences
import aktual.test.runDatabaseTest
import androidx.compose.ui.graphics.Color
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.assertions.prop
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditTagViewModelTest {
  private lateinit var tagsDao: TagsDao

  @Test
  fun `New tag starts as an empty editing state`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope, id = null)

    viewModel.state.test {
      assertThat(awaitEditing()).isEqualTo(EditTagState.Editing(isNew = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `New tag seeds its colour from the last used colour`() = runDatabaseTest { scope ->
    val preferences = TagPreferencesImpl(scope.buildPreferences())
    preferences.lastUsedTagColor.set("#AABBCC")

    val viewModel = createViewModel(scope, id = null, preferences = preferences)

    viewModel.state.test {
      assertThat(awaitEditing())
        .isEqualTo(EditTagState.Editing(color = Color(0xFFAABBCC), isNew = true))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Loads an existing tag from the database`() = runDatabaseTest { scope ->
    insertTag(
      id = "groceries-id",
      tag = "groceries",
      color = "#aabbcc",
      description = "Weekly food shopping",
    )

    val viewModel = createViewModel(scope, id = TagId("groceries-id"))

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
  fun `Requesting a tag that doesn't exist surfaces a failure`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope, id = TagId("missing-id"))

    viewModel.state.test {
      assertThat(awaitFailure()).isEqualTo(EditTagState.Failure(cause = null))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Tracks unsaved changes against the loaded values`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    val viewModel = createViewModel(scope, id = TagId("groceries-id"))
    viewModel.awaitLoaded()

    viewModel.hasUnsavedChanges.test {
      assertThatNextEmission().isFalse()

      viewModel.setTag("groceries-changed")
      assertThatNextEmission().isTrue()

      // editing back to the original value clears the unsaved-changes flag
      viewModel.setTag("groceries")
      assertThatNextEmission().isFalse()
    }
  }

  @Test
  fun `Cannot save a blank tag or one with an invalid colour`() = runDatabaseTest { scope ->
    val viewModel = createViewModel(scope, id = null)
    viewModel.awaitLoaded()

    viewModel.canSave.test {
      assertThatNextEmission().isFalse()

      viewModel.setTag("groceries")
      assertThatNextEmission().isTrue()

      viewModel.setColorError(isError = true)
      assertThatNextEmission().isFalse()
    }
  }

  @Test
  fun `Cannot save a new tag whose name already exists`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    val viewModel = createViewModel(scope, id = null)
    viewModel.awaitLoaded()

    viewModel.canSave.test {
      assertThatNextEmission().isFalse()

      // a fresh, unused name is saveable
      viewModel.setTag("food")
      assertThatNextEmission().isTrue()

      // an existing name blocks saving
      viewModel.setTag("groceries")
      assertThatNextEmission().isFalse()
    }
  }

  @Test
  fun `Editing a tag without renaming it doesn't count as a duplicate`() =
    runDatabaseTest { scope ->
      insertTag(id = "groceries-id", tag = "groceries")
      insertTag(id = "food-id", tag = "food")
      val viewModel = createViewModel(scope, id = TagId("groceries-id"))
      viewModel.awaitLoaded()

      viewModel.isDuplicateName.test {
        // keeping its own name is fine
        assertThatNextEmission().isFalse()

        // renaming onto another existing tag is blocked
        viewModel.setTag("food")
        assertThatNextEmission().isTrue()
      }
    }

  @Test
  fun `Saving a new tag persists it, syncs the change and emits the saved event`() =
    runDatabaseTest { scope ->
      val sync = TestSyncController()
      val viewModel = createViewModel(scope, id = null, sync = sync)
      viewModel.awaitLoaded()

      viewModel.setTag("groceries")
      viewModel.setDescription("Weekly food shopping")
      viewModel.setColor(Color(0xFFAABBCC))

      viewModel.events.test {
        viewModel.save()
        assertThatNextEmission().isEqualTo(EditTagEvent.FinishedSaving)
      }

      val saved = tagsDao.getTag(TagId(GENERATED_ID))
      assertThat(saved).isNotNull().all {
        prop(GetTag::tag).isEqualTo("groceries")
        prop(GetTag::color).isEqualTo("#AABBCC")
        prop(GetTag::description).isEqualTo("Weekly food shopping")
      }

      assertThat(sync.changes).isNotEmpty()
    }

  @Test
  fun `Saving a coloured tag remembers the colour for the next new tag`() =
    runDatabaseTest { scope ->
      val preferences = TagPreferencesImpl(scope.buildPreferences())
      val viewModel = createViewModel(scope, id = null, preferences = preferences)
      viewModel.awaitLoaded()

      viewModel.setTag("groceries")
      viewModel.setColor(Color(0xFFAABBCC))

      viewModel.events.test {
        viewModel.save()
        assertThatNextEmission().isEqualTo(EditTagEvent.FinishedSaving)
      }

      assertThat(preferences.lastUsedTagColor.get()).isEqualTo("#AABBCC")
    }

  @Test
  fun `Saving edits to an existing tag updates the row and syncs the change`() =
    runDatabaseTest { scope ->
      insertTag(
        id = "groceries-id",
        tag = "groceries",
        color = "#aabbcc",
        description = "Weekly food shopping",
      )

      val sync = TestSyncController()
      val viewModel = createViewModel(scope, id = TagId("groceries-id"), sync = sync)
      viewModel.awaitLoaded()

      viewModel.setTag("food")
      viewModel.setDescription("Food and household")
      viewModel.setColor(Color(0xFF112233))

      viewModel.events.test {
        viewModel.save()
        assertThatNextEmission().isEqualTo(EditTagEvent.FinishedSaving)
      }

      // the existing row is updated in place rather than duplicated or rejected
      val tags = tagsDao.getTags()
      assertThat(tags).hasSize(1)
      val saved = tagsDao.getTag(TagId("groceries-id"))
      assertThat(saved).isNotNull().all {
        prop(GetTag::tag).isEqualTo("food")
        prop(GetTag::color).isEqualTo("#112233")
        prop(GetTag::description).isEqualTo("Food and household")
      }

      assertThat(sync.changes).isNotEmpty()
    }

  @Test
  fun `Creating a tag whose name matches a tombstoned tag resurrects the old id`() =
    runDatabaseTest { scope ->
      insertTag(id = "old-id", tag = "groceries")
      // tombstone it, as a delete-sync would
      tombstoneTag("old-id")

      val sync = TestSyncController()
      val viewModel = createViewModel(scope, id = null, sync = sync)
      viewModel.awaitLoaded()

      viewModel.setTag("groceries")
      viewModel.events.test {
        viewModel.save()
        assertThatNextEmission().isEqualTo(EditTagEvent.FinishedSaving)
      }

      // the old row's id is reused rather than the freshly generated uuid
      assertThat(tagsDao.getTagIdByName("groceries")).isEqualTo(TagId("old-id"))
      assertThat(sync.changes.map(LocalChange::row).distinct()).containsExactly("old-id")
    }

  @Test
  fun `Renaming a tag onto a tombstoned name resurrects the old row and retires the edited one`() =
    runDatabaseTest { scope ->
      insertTag(id = "old-id", tag = "groceries")
      // tombstone "groceries", as a delete-sync would, so it still owns the UNIQUE name
      tombstoneTag("old-id")
      insertTag(id = "food-id", tag = "food")

      val sync = TestSyncController()
      val viewModel = createViewModel(scope, id = TagId("food-id"), sync = sync)
      viewModel.awaitLoaded()

      // renaming onto the deleted name must not trip the UNIQUE(tag) constraint — before the fix
      // this insert threw and save() surfaced an error instead of finishing. FinishedSaving is only
      // emitted on the happy path, so receiving it proves the save succeeded
      viewModel.setTag("groceries")
      viewModel.setDescription("Weekly food shopping")

      viewModel.events.test {
        viewModel.save()
        assertThatNextEmission().isEqualTo(EditTagEvent.FinishedSaving)
      }

      // the name now resolves to the resurrected row rather than the one we were editing
      assertThat(tagsDao.getTagIdByName("groceries")).isEqualTo(TagId("old-id"))

      // the sync log retires the edited row and rewrites the deleted one under its original id
      assertThat(sync.changes.map(LocalChange::row).distinct())
        .containsExactlyInAnyOrder("food-id", "old-id")
      assertThat(sync.changes.filter { it.row == "food-id" }.map(LocalChange::column))
        .containsExactly("tombstone")
      // the resurrected row gets a full insert — the edits must not be dropped, and tombstone is
      // cleared to bring it back
      assertThat(sync.changes.filter { it.row == "old-id" }.map(LocalChange::column))
        .containsExactlyInAnyOrder("id", "tag", "color", "description", "tombstone")
      val oldIdChanges = sync.changes.filter { it.row == "old-id" }.associateBy(LocalChange::column)
      assertThat(oldIdChanges["tag"]?.value).isEqualTo(MessageValue.String("groceries"))
      assertThat(oldIdChanges["description"]?.value)
        .isEqualTo(MessageValue.String("Weekly food shopping"))
      assertThat(oldIdChanges["tombstone"]?.value).isEqualTo(MessageValue.Number(0))
    }

  @Test
  fun `A failed save surfaces an error instead of finishing`() = runDatabaseTest { scope ->
    insertTag(id = "groceries-id", tag = "groceries")
    insertTag(id = "food-id", tag = "food")
    val viewModel = createViewModel(scope, id = TagId("groceries-id"))
    viewModel.awaitLoaded()

    // renaming onto another tag's name trips the UNIQUE(tag) constraint; calling save() directly
    // bypasses the disabled button, so the error must surface rather than be silently swallowed
    viewModel.setTag("food")

    viewModel.saveError.test {
      assertThatNextEmission().isNull()
      viewModel.save()
      assertThatNextEmission().isNotNull()
    }
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
    scope: TestScope,
    id: TagId?,
    uuid: UuidGenerator = UuidGenerator { GENERATED_ID },
    sync: BudgetSyncController = TestSyncController(),
    preferences: TagPreferences = TagPreferencesImpl(scope.buildPreferences()),
  ): EditTagViewModel {
    tagsDao = TagsDao(this)
    return EditTagViewModel(
      tagId = id,
      tagsDao = tagsDao,
      uuidGenerator = uuid,
      syncController = sync,
      preferences = preferences,
    )
  }

  private companion object {
    const val GENERATED_ID = "generated-id"
  }
}
