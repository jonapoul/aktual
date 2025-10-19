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
package aktual.settings.ui.items

import aktual.core.model.DarkColorSchemeType
import aktual.core.model.RegularColorSchemeType
import aktual.core.ui.AktualTypography
import aktual.core.ui.AlertDialog
import aktual.core.ui.ButtonShape
import aktual.core.ui.LocalTheme
import aktual.core.ui.Theme
import aktual.l10n.Strings
import aktual.settings.ui.BasicPreferenceItem
import aktual.settings.ui.NotClickable
import aktual.settings.vm.ThemeConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import dev.chrisbanes.haze.HazeState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun ThemePreferenceItem(
  config: ThemeConfig,
  onChange: (ThemeConfig) -> Unit,
  modifier: Modifier = Modifier,
  hazeState: HazeState = remember { HazeState() },
) {
  var openRegularDialog by remember { mutableStateOf(false) }
  var openDarkDialog by remember { mutableStateOf(false) }
  val (theme, darkTheme) = config

  Column(
    modifier = modifier.fillMaxWidth(),
  ) {
    BasicPreferenceItem(
      title = Strings.settingsTheme,
      subtitle = null,
      icon = theme.icon(),
      clickability = NotClickable,
      hazeState = hazeState,
    ) {
      TypeButton(
        modifier = Modifier
          .fillMaxWidth()
          .padding(3.dp),
        type = theme,
        isSelected = false,
        strings = RegularSchemeTypeStrings,
        onClick = { openRegularDialog = !openRegularDialog },
      )
    }

    if (config.regular in OPTIONS_TO_SHOW_DARK_CONFIG) {
      BasicPreferenceItem(
        title = Strings.settingsDarkTheme,
        subtitle = null,
        icon = darkTheme.icon(),
        clickability = NotClickable,
        hazeState = hazeState,
      ) {
        TypeButton(
          modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
          type = darkTheme,
          isSelected = false,
          strings = DarkSchemeTypeStrings,
          onClick = { openDarkDialog = !openDarkDialog },
        )
      }
    }
  }

  if (openRegularDialog) {
    ThemeChooserDialog(
      selected = theme,
      options = REGULAR_OPTIONS,
      strings = RegularSchemeTypeStrings,
      onDismissRequest = { openRegularDialog = false },
      onSelect = { newValue ->
        openRegularDialog = false
        onChange(config.copy(regular = newValue))
      },
    )
  }

  if (openDarkDialog) {
    ThemeChooserDialog(
      selected = darkTheme,
      options = DARK_OPTIONS,
      strings = DarkSchemeTypeStrings,
      onDismissRequest = { openDarkDialog = false },
      onSelect = { newValue ->
        openDarkDialog = false
        onChange(config.copy(dark = newValue))
      },
    )
  }
}

@Composable
private fun <T> ThemeChooserDialog(
  selected: T,
  options: ImmutableList<T>,
  strings: SchemeTypeStrings<T>,
  onSelect: (T) -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
) = AlertDialog(
  modifier = modifier,
  title = Strings.settingsTheme,
  onDismissRequest = onDismissRequest,
  buttons = {
    TextButton(onClick = onDismissRequest) {
      Text(text = Strings.settingsDialogDismiss)
    }
  },
  content = {
    ThemeChooserDialogContent(
      selected = selected,
      options = options,
      strings = strings,
      onSelect = onSelect,
    )
  },
)

private val REGULAR_OPTIONS = RegularColorSchemeType.entries.toImmutableList()
private val DARK_OPTIONS = DarkColorSchemeType.entries.toImmutableList()
private val OPTIONS_TO_SHOW_DARK_CONFIG = persistentSetOf(
  RegularColorSchemeType.Dark,
  RegularColorSchemeType.System,
)

@Composable
private fun <T> ThemeChooserDialogContent(
  selected: T,
  strings: SchemeTypeStrings<T>,
  options: ImmutableList<T>,
  onSelect: (T) -> Unit,
) {
  options.fastForEach { type ->
    TypeButton(
      modifier = Modifier
        .fillMaxWidth()
        .padding(1.dp),
      type = type,
      isSelected = selected == type,
      strings = strings,
      onClick = { onSelect(type) },
    )
  }
}

@Composable
private fun <T> TypeButton(
  type: T,
  isSelected: Boolean,
  strings: SchemeTypeStrings<T>,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val buttonColors = theme.colors(isPressed, isSelected)
  val background = buttonColors.containerColor

  TextButton(
    modifier = modifier.background(background, ButtonShape),
    onClick = onClick,
    enabled = true,
    colors = buttonColors,
  ) {
    Text(
      text = strings(type),
      textAlign = TextAlign.Center,
      style = AktualTypography.bodySmall,
    )
  }
}

@Stable
@Composable
private fun Theme.colors(
  isPressed: Boolean,
  isSelected: Boolean,
) = ButtonDefaults.textButtonColors(
  containerColor = when {
    isPressed -> buttonPrimaryBackgroundHover
    isSelected -> buttonPrimaryBackground
    else -> buttonNormalBackground
  },
  contentColor = when {
    isPressed -> buttonPrimaryTextHover
    isSelected -> buttonPrimaryText
    else -> buttonNormalText
  },
  disabledContainerColor = buttonPrimaryDisabledBackground,
  disabledContentColor = buttonPrimaryDisabledText,
)

@Immutable
private fun interface SchemeTypeStrings<T> {
  @ReadOnlyComposable
  @Composable
  operator fun invoke(value: T): String
}

private val DarkSchemeTypeStrings = SchemeTypeStrings<DarkColorSchemeType> { type ->
  when (type) {
    DarkColorSchemeType.Dark -> Strings.themeDark
    DarkColorSchemeType.Midnight -> Strings.themeMidnight
  }
}

private val RegularSchemeTypeStrings = SchemeTypeStrings<RegularColorSchemeType> { type ->
  when (type) {
    RegularColorSchemeType.System -> Strings.themeSystem
    RegularColorSchemeType.Light -> Strings.themeLight
    RegularColorSchemeType.Dark -> Strings.themeDark
  }
}

@Stable
private fun RegularColorSchemeType.icon(): ImageVector = when (this) {
  RegularColorSchemeType.System -> Icons.Filled.Settings
  RegularColorSchemeType.Light -> Icons.Filled.LightMode
  RegularColorSchemeType.Dark -> Icons.Filled.Brightness2
}

@Stable
private fun DarkColorSchemeType.icon(): ImageVector = when (this) {
  DarkColorSchemeType.Dark -> Icons.Filled.Brightness2
  DarkColorSchemeType.Midnight -> Icons.Filled.Brightness3
}
