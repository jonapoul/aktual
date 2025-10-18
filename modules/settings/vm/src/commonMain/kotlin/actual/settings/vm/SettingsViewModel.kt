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

import actual.core.di.ViewModelKey
import actual.core.di.ViewModelScope
import actual.prefs.AppGlobalPreferences
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.jonpoulton.preferences.core.asStateFlow
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

@Inject
@ViewModelKey(SettingsViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SettingsViewModel internal constructor(
  preferences: AppGlobalPreferences,
) : ViewModel() {
  private val colorSchemePref = preferences.regularColorScheme
  private val colorSchemeFlow = colorSchemePref.asStateFlow(viewModelScope)

  private val darkSchemePref = preferences.darkColorScheme
  private val darkSchemeFlow = darkSchemePref.asStateFlow(viewModelScope)

  private val showBottomBarPref = preferences.showBottomBar
  private val showBottomBar = showBottomBarPref.asStateFlow(viewModelScope)

  val prefValues: StateFlow<ImmutableList<PreferenceValue>> = viewModelScope.launchMolecule(Immediate) {
    val colorScheme by colorSchemeFlow.collectAsState()
    val darkScheme by darkSchemeFlow.collectAsState()
    val showBottomBar by showBottomBar.collectAsState()
    persistentListOf(
      PreferenceValue.Theme(ThemeConfig(colorScheme, darkScheme)),
      PreferenceValue.ShowBottomBar(showBottomBar),
    )
  }

  fun set(value: PreferenceValue) {
    when (value) {
      is PreferenceValue.Theme -> setColorScheme(value.config)
      is PreferenceValue.ShowBottomBar -> showBottomBarPref.set(value.show)
    }
  }

  private fun setColorScheme(config: ThemeConfig) {
    colorSchemePref.set(config.regular)
    darkSchemePref.set(config.dark)
  }
}
