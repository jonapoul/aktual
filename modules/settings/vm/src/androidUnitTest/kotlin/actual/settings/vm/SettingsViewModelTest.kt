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
package actual.settings.vm

import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import actual.prefs.AppGlobalPreferences
import actual.settings.vm.PreferenceValue.ShowBottomBar
import actual.settings.vm.PreferenceValue.Theme
import actual.test.buildPreferences
import alakazam.test.core.standardDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {
  private lateinit var viewModel: SettingsViewModel

  private fun TestScope.before() {
    val dispatcher = standardDispatcher
    val prefs = buildPreferences(dispatcher)

    viewModel = SettingsViewModel(
      preferences = AppGlobalPreferences(prefs),
    )
  }

  @Test
  fun `Color scheme changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.System, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark))
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Dark))
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Dark))

      viewModel.set(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Midnight))
      assertThat(awaitItem()).contains(theme(RegularColorSchemeType.Light, DarkColorSchemeType.Midnight))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Bottom bar preference changes`() = runTest {
    before()
    viewModel.prefValues.test {
      assertThat(awaitItem()).contains(ShowBottomBar(true))

      viewModel.set(ShowBottomBar(false))
      assertThat(awaitItem()).contains(ShowBottomBar(false))

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun theme(regular: RegularColorSchemeType, dark: DarkColorSchemeType) = Theme(ThemeConfig(regular, dark))
}
