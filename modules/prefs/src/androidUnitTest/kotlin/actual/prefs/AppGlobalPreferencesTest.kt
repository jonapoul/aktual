/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.prefs

import actual.core.model.Protocol
import actual.core.model.RegularColorSchemeType.Dark
import actual.core.model.RegularColorSchemeType.Light
import actual.core.model.RegularColorSchemeType.System
import actual.core.model.ServerUrl
import actual.test.assertThatNextEmissionIsEqualTo
import actual.test.buildPreferences
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
