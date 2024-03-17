package dev.jonpoulton.actual.serverurl.prefs

import alakazam.test.core.CoroutineRule
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import dev.jonpoulton.actual.serverurl.model.Protocol
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
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sharedPrefs = context.getSharedPreferences("actual", Context.MODE_PRIVATE)
    val flowPrefs = FlowSharedPreferences(sharedPrefs, coroutineRule.dispatcher)
    preferences = ServerUrlPreferences(flowPrefs)
  }

  @Test
  fun `Null initial states`() {
    assertNull(preferences.url.get())
    assertNull(preferences.protocol.get())
  }

  @Test
  fun `Emit URL`() = runTest {
    preferences.url.asFlow().test {
      // Given
      assertNull(awaitItem())

      // When
      val url = "website.com"
      preferences.url.set(url)

      // Then
      assertEquals(expected = url, actual = awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Emit protocol`() = runTest {
    preferences.protocol.asFlow().test {
      // Given
      assertNull(awaitItem())

      // When
      val protocol = Protocol.Https
      preferences.protocol.set(protocol)

      // Then
      assertEquals(expected = protocol, actual = awaitItem())
      cancelAndIgnoreRemainingEvents()
    }
  }
}
