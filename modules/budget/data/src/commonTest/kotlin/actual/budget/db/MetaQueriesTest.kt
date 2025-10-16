package actual.budget.db

import actual.budget.db.test.getMetaValue
import actual.budget.db.test.insertMeta
import actual.test.runDatabaseTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MetaQueriesTest {
  @Test
  fun `Getting from empty table returns null`() = runDatabaseTest {
    assertNull(getMetaValue(key = "test"))
  }

  @Test
  fun `Inserting then getting`() = runDatabaseTest {
    assertNull(getMetaValue(key = "myKey"))
    insertMeta(key = "myKey", value = "myValue")
    assertThat(getMetaValue(key = "myKey")).isEqualTo("myValue")
  }
}
