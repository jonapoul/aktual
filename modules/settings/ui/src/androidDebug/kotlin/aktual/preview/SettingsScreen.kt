/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
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
