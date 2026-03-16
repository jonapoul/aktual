package aktual.budget.db

import aktual.budget.db.model.Meta
import aktual.test.runDatabaseTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

internal class MetaQueriesTest {
  @Test
  fun `Getting from empty table returns null`() = runDatabaseTest {
    val db = buildDatabase()
    assertThat(db.meta().getValue(key = "test")).isNull()
  }

  @Test
  fun `Inserting then getting`() = runDatabaseTest {
    val db = buildDatabase()
    assertThat(db.meta().getValue(key = "myKey")).isNull()
    db.meta().insert(Meta(key = "myKey", value = "myValue"))
    assertThat(db.meta().getValue(key = "myKey")).isEqualTo("myValue")
  }
}
