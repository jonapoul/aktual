package aktual.prefs

import aktual.core.model.Protocol
import aktual.core.model.RegularColorSchemeType.Dark
import aktual.core.model.RegularColorSchemeType.Light
import aktual.core.model.RegularColorSchemeType.System
import aktual.core.model.ServerUrl
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import alakazam.test.core.unconfinedDispatcher
import app.cash.turbine.test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

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
        assertThatNextEmissionIsEqualTo(null)

        // When
        val url1 = ServerUrl(protocol = Protocol.Https, baseUrl = "website.com")
        set(url1)

        // Then
        assertThatNextEmissionIsEqualTo(url1)

        // When
        val url2 = ServerUrl(protocol = Protocol.Http, baseUrl = "some.other.domain.co.uk")
        set(url2)

        // Then
        assertThatNextEmissionIsEqualTo(url2)

        // When
        delete()

        // Then
        assertThatNextEmissionIsEqualTo(null)
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
        assertThatNextEmissionIsEqualTo(System)

        set(Light)
        assertThatNextEmissionIsEqualTo(Light)

        set(System)
        assertThatNextEmissionIsEqualTo(System)

        set(Dark)
        assertThatNextEmissionIsEqualTo(Dark)

        deleteAndCommit()
        assertThatNextEmissionIsEqualTo(System)

        expectNoEvents()
        cancelAndIgnoreRemainingEvents()
      }
    }
  }
}
