package actual.db

import actual.test.DatabaseRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MetaQueriesTest {
  @get:Rule
  val databaseRule = DatabaseRule.inMemory()

  @Test
  fun `Getting from empty table returns null`() = runTest {
    assertNull(getValue(key = "test"))
  }

  @Test
  fun `Inserting then getting`() = runTest {
    assertNull(getValue(key = "myKey"))
    insert(key = "myKey", value = "myValue")
    assertEquals(expected = "myValue", actual = getValue(key = "myKey"))
  }

  private suspend fun getValue(key: String): String? =
    databaseRule.database.metaQueries.withResult { getValue(key).executeAsOneOrNull()?.value_ }

  private suspend fun insert(key: String, value: String) =
    databaseRule.database.metaQueries.withoutResult { insert(key, value) }
}
