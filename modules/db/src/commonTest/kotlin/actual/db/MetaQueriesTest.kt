package actual.db

import actual.db.test.getMetaValue
import actual.db.test.insertMeta
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class MetaQueriesTest : DatabaseTest() {
  @Test
  fun `Getting from empty table returns null`() = runDatabaseTest {
    assertNull(getMetaValue(key = "test"))
  }

  @Test
  fun `Inserting then getting`() = runDatabaseTest {
    assertNull(getMetaValue(key = "myKey"))
    insertMeta(key = "myKey", value = "myValue")
    assertEquals(expected = "myValue", actual = getMetaValue(key = "myKey"))
  }
}
