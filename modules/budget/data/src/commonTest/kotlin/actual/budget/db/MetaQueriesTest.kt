package actual.budget.db

import actual.budget.db.test.getMetaValue
import actual.budget.db.test.insertMeta
import actual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

internal class MetaQueriesTest {
  @Test
  fun `Getting from empty table returns null`() = runDatabaseTest {
    assertThat(getMetaValue(key = "test")).isNull()
  }

  @Test
  fun `Inserting then getting`() = runDatabaseTest {
    assertThat(getMetaValue(key = "myKey")).isNull()
    insertMeta(key = "myKey", value = "myValue")
    assertThat(getMetaValue(key = "myKey")).isEqualTo("myValue")
  }
}
