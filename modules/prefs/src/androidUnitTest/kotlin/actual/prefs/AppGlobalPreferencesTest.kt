package actual.prefs

import actual.core.model.ColorSchemeType
import actual.core.model.ColorSchemeType.Dark
import actual.core.model.ColorSchemeType.Light
import actual.core.model.Protocol
import actual.core.model.ServerUrl
import actual.test.assertEmitted
import actual.test.buildPreferences
import alakazam.test.core.MainDispatcherRule
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class AppGlobalPreferencesTest {
  @get:Rule val mainDispatcherRule = MainDispatcherRule()

  private lateinit var preferences: AppGlobalPreferences

  @BeforeTest
  fun before() {
    val prefs = buildPreferences(mainDispatcherRule.dispatcher)
    preferences = AppGlobalPreferences(prefs)
  }

  @Test
  fun `Server URL`() = runTest {
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
    with(preferences.colorSchemeType) {
      asFlow().test {
        // Given
        assertEmitted(ColorSchemeType.System)

        set(Light)
        assertEmitted(Light)

        set(ColorSchemeType.System)
        assertEmitted(ColorSchemeType.System)

        set(Dark)
        assertEmitted(Dark)

        deleteAndCommit()
        assertEmitted(ColorSchemeType.System)

        expectNoEvents()
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
