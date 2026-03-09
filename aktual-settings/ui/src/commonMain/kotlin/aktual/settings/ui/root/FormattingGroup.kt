package aktual.settings.ui.root

import aktual.budget.model.NumberFormat
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Speed125
import aktual.core.l10n.Strings
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.settings.ui.ListPreferenceItem
import aktual.settings.ui.PreferenceGroup
import aktual.settings.vm.root.NumberFormatPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun FormattingGroup(
  format: NumberFormatPreference,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(title = Strings.settingsFormatGroup, subtitle = null, modifier = modifier) {
    NumberFormat(format, onAction)
  }
}

@Composable
private fun NumberFormat(
  format: NumberFormatPreference,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  ListPreferenceItem(
    modifier = modifier,
    value = format.selected,
    options = format.values,
    optionString = NumberFormat::string,
    optionIcon = null,
    onValueChange = { f -> onAction(SettingsAction.SetNumberFormat(f)) },
    title = Strings.settingsFormatNumbers,
    subtitle = null,
    includeBackground = false,
    icon = MaterialIcons.Speed125,
    enabled = format.enabled,
  )
}

@Stable
private fun NumberFormat.string(): String =
  when (this) {
    NumberFormat.CommaDot -> "1,000.33"
    NumberFormat.DotComma -> "1.000,33"
    NumberFormat.SpaceComma -> "1 000,33"
    NumberFormat.ApostropheDot -> "1'000.33"
    NumberFormat.CommaDotIn -> "1,00,000.33"
  }

@Preview
@Composable
private fun PreviewFormattingGroup(
  @PreviewParameter(FormattingGroupProvider::class) params: ThemedParams<FormattingGroupState>
) =
  PreviewWithColorScheme(params.theme) {
    FormattingGroup(format = NumberFormatPreference(params.data.format), onAction = {})
  }

private data class FormattingGroupState(val format: NumberFormat)

private class FormattingGroupProvider :
  ThemedParameterProvider<FormattingGroupState>(
    FormattingGroupState(format = NumberFormat.CommaDot)
  )
