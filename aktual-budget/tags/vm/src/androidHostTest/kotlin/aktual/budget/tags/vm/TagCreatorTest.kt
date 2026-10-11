package aktual.budget.tags.vm

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.dao.DatabaseTables.TAGS
import aktual.budget.db.dao.TagsDao
import aktual.budget.model.LocalChange
import aktual.budget.model.MessageValue
import aktual.budget.model.TagId
import aktual.test.TestSyncController
import aktual.test.runDatabaseTest
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.TestScope
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TagCreatorTest {
  @Test
  fun `A new tag gets a new id`() = runDatabaseTest { scope ->
    val sync = TestSyncController()

    assertThat(creator(scope, sync).create("food")).isEqualTo(TagId(GENERATED_ID))
    assertThat(sync.changes).isEqualTo(changes(GENERATED_ID, "food"))
  }

  @Test
  fun `A deleted tag with the name is brought back under its old id`() = runDatabaseTest { scope ->
    insertTag(id = "old-id", tag = "food", color = "#FF0000", description = "Meals")
    tombstoneTag("old-id")
    val sync = TestSyncController()

    assertThat(creator(scope, sync).create("food")).isEqualTo(TagId("old-id"))
    assertThat(sync.changes).isEqualTo(changes("old-id", "food"))
  }

  @Test
  fun `A tag with the name is kept as it is`() = runDatabaseTest { scope ->
    insertTag(id = "old-id", tag = "food", color = "#FF0000", description = "Meals")
    val sync = TestSyncController()

    assertThat(creator(scope, sync).create("food")).isEqualTo(TagId("old-id"))
    assertThat(sync.changes).isEmpty()
    assertThat(tagsDao(scope).getTag(TagId("old-id"))?.color).isEqualTo("#FF0000")
  }

  private fun changes(id: String, tag: String) =
    listOf(
      LocalChange(TAGS, id, "id", MessageValue.String(id)),
      LocalChange(TAGS, id, "tag", MessageValue.String(tag)),
      LocalChange(TAGS, id, "color", Null),
      LocalChange(TAGS, id, "description", Null),
      LocalChange(TAGS, id, "tombstone", MessageValue.Number(0)),
    )

  private fun BudgetDatabase.tagsDao(scope: TestScope) =
    TagsDao(this, TestCoroutineContexts(scope.standardDispatcher))

  private fun BudgetDatabase.creator(scope: TestScope, sync: TestSyncController) =
    TagCreator(tagsDao = tagsDao(scope), uuidGenerator = { GENERATED_ID }, syncController = sync)

  private companion object {
    const val GENERATED_ID = "generated-id"
  }
}
