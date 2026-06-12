package aktual.budget.db

import aktual.budget.model.BudgetId
import aktual.budget.model.TagId
import aktual.test.inMemoryDriverFactory
import aktual.test.runDatabaseTest
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

// The `insert` query is an upsert (INSERT ... ON CONFLICT(id) DO UPDATE) rather than an
// INSERT OR REPLACE. These tests pin that down: re-inserting an existing id updates the four
// listed columns while leaving hidden/tombstone alone, so a sync can't un-hide or resurrect a tag
internal class TagsTest {
  @Test
  fun `Inserting a new tag stores it with default flags`() = runDatabaseTest {
    tagsQueries.insert(
      id = TagId("tag-1"),
      tag = "groceries",
      color = "#aabbcc",
      description = "Food shopping",
    )

    // getTags filters out tombstoned rows, so a freshly inserted tag shows up with hidden = false
    assertThat(tagsQueries.getTags().awaitAsList())
      .containsExactly(
        GetTags(
          id = TagId("tag-1"),
          tag = "groceries",
          color = "#aabbcc",
          description = "Food shopping",
          hidden = false,
        )
      )
  }

  @Test
  fun `Re-inserting an existing tag updates its columns but keeps it hidden`() =
    runRawDatabaseTest { driver, db ->
      db.tagsQueries.insert(id = ID, tag = "groceries", color = "#aabbcc", description = "Food")

      // hide the tag out of band, as a hide action would
      driver.execute(null, "UPDATE tags SET hidden = 1 WHERE id = 'tag-1'", 0).await()

      // an incoming change for the same id re-inserts it with new values
      db.tagsQueries.insert(id = ID, tag = "shopping", color = "#ddeeff", description = "Household")

      // the listed columns are updated, but hidden is left untouched (not reset to its default)
      assertThat(db.tagsQueries.getTags().awaitAsList())
        .containsExactly(
          GetTags(
            id = ID,
            tag = "shopping",
            color = "#ddeeff",
            description = "Household",
            hidden = true,
          )
        )
    }

  @Test
  fun `Re-inserting a tombstoned tag does not resurrect it`() = runRawDatabaseTest { driver, db ->
    db.tagsQueries.insert(id = ID, tag = "groceries", color = "#aabbcc", description = "Food")

    // tombstone the tag, as a delete-sync would
    driver.execute(null, "UPDATE tags SET tombstone = 1 WHERE id = 'tag-1'", 0).await()

    // re-inserting the same id must not clear the tombstone back to its default
    db.tagsQueries.insert(id = ID, tag = "shopping", color = "#ddeeff", description = "Household")

    // still tombstoned, so it stays out of getTags
    assertThat(db.tagsQueries.getTags().awaitAsList()).isEmpty()
    assertThat(db.tagsQueries.getHidden(ID).awaitAsOneOrNull()?.hidden).isEqualTo(false)
  }

  // The hidden/tombstone columns have no insert query, so these tests drop down to the driver to
  // seed them - getTags/getHidden aside, the in-memory setup mirrors runDatabaseTest
  private fun runRawDatabaseTest(action: suspend (SqlDriver, BudgetDatabase) -> Unit) = runTest {
    val driver = inMemoryDriverFactory().create(BudgetId("abc-123"))
    val db = buildDatabase(driver)
    driver.use { action(driver, db) }
  }

  private companion object {
    val ID = TagId("tag-1")
  }
}
