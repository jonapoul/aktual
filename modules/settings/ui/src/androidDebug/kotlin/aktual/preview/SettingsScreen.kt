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
package aktual.preview

import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import aktual.core.ui.PreviewThemedScreen
import aktual.core.ui.TripleScreenPreview
import aktual.settings.ui.SettingsScaffold
import aktual.settings.vm.PreferenceValue
import aktual.settings.vm.ThemeConfig
import androidx.compose.runtime.Composable
import kotlinx.collections.immutable.persistentListOf

@TripleScreenPreview
@Composable
private fun Regular() = PreviewThemedScreen { type ->
  SettingsScaffold(
    onAction = {},
    values = persistentListOf(
      PreferenceValue.Theme(ThemeConfig(RegularColorSchemeType.Dark, DarkColorSchemeType.Dark)),
      PreferenceValue.ShowBottomBar(false),
    ),
  )
}
