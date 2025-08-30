package actual.prefs

import actual.core.model.Protocol
import actual.core.model.RegularColorSchemeType.Dark
import actual.core.model.RegularColorSchemeType.Light
import actual.core.model.RegularColorSchemeType.System
import actual.core.model.ServerUrl
import actual.test.assertEmitted
import actual.test.buildPreferences
import alakazam.test.core.unconfinedDispatcher
import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class AppGlobalPreferencesTest {
  private lateinit var preferences: AppGlobalPreferences

  private fun TestScope.before() {
    val prefs = buildPreferences(unconfinedDispatcher)
    preferences = AppGlobalPreferences(prefs)
  }

  @Test
  fun `Server URL`() = runTest {
    before()
    with(preferences.serverUrl) {
      asFlow().test {
        // Given
        assertNull(awaitItem())

        // When
        val url1 = ServerUrl(protocol = Protocol.Https, baseUrl = "website.com")
        set(url1)

        // Then
        assertEquals(expected = url1, actual = awaitItem())

        // When
        val url2 = ServerUrl(protocol = Protocol.Http, baseUrl = "some.other.domain.co.uk")
        set(url2)

        // Then
        assertEquals(expected = url2, actual = awaitItem())

        // When
        delete()

        // Then
        assertNull(awaitItem())
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `Colour scheme types`() = runTest {
    before()
    with(preferences.regularColorScheme) {
      asFlow().test {
        // Given
        assertEmitted(System)

        set(Light)
        assertEmitted(Light)

        set(System)
        assertEmitted(System)

        set(Dark)
        assertEmitted(Dark)

        deleteAndCommit()
        assertEmitted(System)

        expectNoEvents()
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
