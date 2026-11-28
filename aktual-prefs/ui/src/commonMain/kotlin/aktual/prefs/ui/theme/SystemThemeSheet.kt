package aktual.prefs.ui.theme

import aktual.core.l10n.Strings
import aktual.core.model.ThemeId
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Chip
import aktual.core.ui.ColorParameters
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.disabled
import aktual.core.ui.radioButton
import aktual.prefs.vm.theme.DownloadState
import aktual.prefs.vm.theme.SystemThemePicker
import aktual.prefs.vm.theme.ThemeOption
import aktual.prefs.vm.theme.palette
import aktual.theme.Colors
import aktual.theme.DarkColors
import aktual.theme.MidnightColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

// Picking a catalog theme downloads it first, and the sheet closes once it's set
@Composable
internal fun SystemThemeSheet(
  type: PickerType,
  picker: SystemThemePicker,
  customThemeNames: ImmutableMap<ThemeId, String>,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  AktualModalBottomSheet(modifier = modifier, onDismissRequest = { onAction(DismissPicker) }) {
    SystemThemeSheetContent(type, picker, customThemeNames, onAction)
  }
}

@Composable
private fun SystemThemeSheetContent(
  type: PickerType,
  picker: SystemThemePicker,
  customThemeNames: ImmutableMap<ThemeId, String>,
  onAction: OnAction<ThemeSettingsAction>,
  modifier: Modifier = Modifier,
) {
  val isBusy = picker.options.any { it.download == Downloading }

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp),
      text = type.title(),
      style = typography.titleMedium,
      color = colors.text,
    )

    LazyColumn(
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      items(picker.options, key = { it.id.value }) { option ->
        OptionItem(
          option = option,
          name = option.name ?: themeName(option.id, customThemeNames),
          isSelected = option.id == picker.value,
          enabled = !isBusy,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun OptionItem(
  option: ThemeOption,
  name: String,
  isSelected: Boolean,
  enabled: Boolean,
  onAction: OnAction<ThemeSettingsAction>,
) {
  val background by
    animateColorAsState(
      if (isSelected) colors.accentSubtle else Color.Transparent,
    )

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .heightIn(min = 56.dp)
        .clip(RowShape)
        .background(background, RowShape)
        .selectable(
          selected = isSelected,
          enabled = enabled,
          role = Role.RadioButton,
          onClick = { onAction(PickTheme(option.id)) },
        )
        .padding(horizontal = 4.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = CenterVertically,
  ) {
    RadioButton(
      modifier = Modifier.padding(8.dp),
      enabled = enabled,
      selected = isSelected,
      onClick = null,
      colors = colors.radioButton(),
    )

    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f, fill = false),
          text = name,
          style = typography.bodyLarge,
          fontWeight = if (isSelected) Medium else Normal,
          maxLines = 1,
          overflow = Ellipsis,
          color =
            when {
              !enabled -> colors.text.disabled
              isSelected -> colors.accentSubtleText
              else -> colors.text
            },
        )

        if (option.isLocal) {
          Chip(text = Strings.settingsThemeLocalBadge, size = Small)
        }
      }

      Palette(option.palette, enabled)
    }

    option.download?.let { state -> DownloadStateIcon(name, state, onAction) }
  }
}

@Preview
@Composable
private fun PreviewSystemThemeSheetContent(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    SystemThemeSheetContent(
      type = DarkTheme,
      customThemeNames = persistentMapOf(),
      onAction = {},
      picker =
        SystemThemePicker(
          value = MidnightColors.id,
          enabled = true,
          options =
            persistentListOf(
              PreviewOption.copy(id = DarkColors.id, name = null, palette = DarkColors.palette()),
              PreviewOption.copy(id = MidnightColors.id, name = null),
              PreviewOption.copy(id = ThemeId("@local/a"), name = "Dark (copy)", isLocal = true),
              PreviewOption.copy(id = ThemeId("a/b"), name = "Remote theme", download = Remote),
              PreviewOption.copy(
                id = ThemeId("a/c"),
                name = "Broken theme",
                download = DownloadState.Failed("Something broke"),
              ),
            ),
        ),
    )
  }

private val PreviewOption =
  ThemeOption(
    id = MidnightColors.id,
    name = null,
    palette = MidnightColors.palette(),
    isLocal = false,
    download = null,
  )
