package dev.jonpoulton.actual.serverurl.prefs

import alakazam.test.core.CoroutineRule
import app.cash.turbine.test
import dev.jonpoulton.actual.core.model.Protocol
import dev.jonpoulton.actual.core.model.ServerUrl
import dev.jonpoulton.actual.test.buildFlowPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricTestRunner::class)
class ServerUrlPreferencesTest {
  @get:Rule
  val coroutineRule = CoroutineRule()

  private lateinit var preferences: ServerUrlPreferences

  @Before
  fun before() {
    val flowPrefs = buildFlowPreferences(coroutineRule.dispatcher)
    preferences = ServerUrlPreferences(flowPrefs)
  }

  @Test
  fun `Emit URLs`() = runTest {
    preferences.url.asFlow().test {
      // Given
      assertNull(awaitItem())

      // When
      val url1 = ServerUrl(protocol = Protocol.Https, baseUrl = "website.com")
      preferences.url.set(url1)

      // Then
      assertEquals(expected = url1, actual = awaitItem())

      // When
      val url2 = ServerUrl(protocol = Protocol.Http, baseUrl = "some.other.domain.co.uk")
      preferences.url.set(url2)

      // Then
      assertEquals(expected = url2, actual = awaitItem())

      // When
      preferences.url.delete()

      // Then
      assertNull(awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }
}
