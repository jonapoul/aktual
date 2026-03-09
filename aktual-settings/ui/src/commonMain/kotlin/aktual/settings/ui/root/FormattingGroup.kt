package aktual.settings.ui.root

import aktual.budget.model.NumberFormat
import aktual.core.icons.DecimalDecrease
import aktual.core.icons.DecimalIncrease
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Speed125
import aktual.core.l10n.Strings
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.settings.ui.BooleanPreferenceItem
import aktual.settings.ui.ListPreferenceItem
import aktual.settings.ui.PreferenceGroup
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.root.NumberFormatPreference
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun FormattingGroup(
  numberFormat: NumberFormatPreference,
  hideFraction: BooleanPreference,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(title = Strings.settingsFormatGroup, subtitle = null, modifier = modifier) {
    NumberFormat(numberFormat, onAction)
    HideFraction(hideFraction, onAction)
  }
}

@Composable
private fun NumberFormat(
  preference: NumberFormatPreference,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  ListPreferenceItem(
    modifier = modifier,
    value = preference.selected,
    options = NumberFormatPreference.Values,
    optionString = { f -> f.string() },
    optionIcon = null,
    onValueChange = { f -> onAction(SettingsAction.SetNumberFormat(f)) },
    title = Strings.settingsFormatNumbers,
    subtitle = null,
    includeBackground = false,
    icon = MaterialIcons.Speed125,
    enabled = true,
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

@Composable
private fun HideFraction(
  preference: BooleanPreference,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  BooleanPreferenceItem(
    modifier = modifier,
    value = preference.value,
    onValueChange = { f -> onAction(SettingsAction.SetHideFraction(f)) },
    title = Strings.settingsFormatDecimal,
    subtitle = null,
    includeBackground = false,
    icon = if (preference.value) MaterialIcons.DecimalDecrease else MaterialIcons.DecimalIncrease,
    enabled = preference.enabled,
  )
}

@Preview
@Composable
private fun PreviewFormattingGroup(
  @PreviewParameter(FormattingGroupProvider::class) params: ThemedParams<FormattingGroupState>
) =
  PreviewWithColorScheme(params.theme) {
    FormattingGroup(
      numberFormat = NumberFormatPreference(params.data.numberFormat),
      hideFraction = BooleanPreference(params.data.hideFraction),
      onAction = {},
    )
  }

private data class FormattingGroupState(val numberFormat: NumberFormat, val hideFraction: Boolean)

private class FormattingGroupProvider :
  ThemedParameterProvider<FormattingGroupState>(
    FormattingGroupState(numberFormat = NumberFormat.CommaDot, hideFraction = true)
  )
