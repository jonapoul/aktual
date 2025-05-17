package actual.db

import actual.db.test.assertAllPreferences
import actual.db.test.getPreference
import actual.db.test.setPreference
import actual.test.assertEmitted
import app.cash.sqldelight.coroutines.asFlow
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
internal class PreferencesQueriesTest : DatabaseTest() {
  @Test
  fun `Getting from empty table returns null`() = runDatabaseTest {
    assertNull(getPreference(key = "test"))
  }

  @Test
  fun `Observing by flow`() = runDatabaseTest { scope ->
    val key = "key"
    preferencesQueries
      .getValue(key)
      .asFlow()
      .map { it.executeAsOneOrNull()?.value_ }
      .distinctUntilChanged()
      .test {
        assertEmitted(null)
        setPreference(key, "value1")
        assertEmitted("value1")
        setPreference(key, "value2")
        assertEmitted("value2")

        // changing a separate key doesn't affect it - the distinctUntilChanged is needed here
        setPreference("some-other-key", "whatever")
        scope.advanceUntilIdle()
        expectNoEvents()

        setPreference(key, null)
        assertEmitted(null)
        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `Get all`() = runDatabaseTest {
    assertAllPreferences()

    setPreference("key1", "value1")
    assertAllPreferences(
      Preferences("key1", "value1"),
    )

    setPreference("key2", "value2")
    assertAllPreferences(
      Preferences("key1", "value1"),
      Preferences("key2", "value2"),
    )

    setPreference("key2", "value2.1")
    assertAllPreferences(
      Preferences("key1", "value1"),
      Preferences("key2", "value2.1"),
    )
  }
}
