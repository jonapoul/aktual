package aktual.prefs.ui.format

import aktual.budget.model.Amount
import aktual.core.icons.AktualIcons
import aktual.core.icons.Location
import aktual.core.icons.material.CalendarViewWeek
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Speed125
import aktual.core.l10n.Strings
import aktual.core.model.DistanceUnit
import aktual.core.nav.AmountSettingsNavigator
import aktual.core.nav.BackNavigator
import aktual.core.nav.DateSettingsNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.theme.Colors
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.ui.root.SubSettingsItem
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.format.FormatSettingsState
import aktual.prefs.vm.format.FormatSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate

@Composable
fun FormatSettingsScreen(
  back: BackNavigator,
  toAmountSettings: AmountSettingsNavigator,
  toDateSettings: DateSettingsNavigator,
  highlight: SettingsHighlight?,
  viewModel: FormatSettingsViewModel = metroViewModel<FormatSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  FormatSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = { back() },
    onOpenAmounts = { toAmountSettings() },
    onOpenDates = { toDateSettings() },
  )
}

@Composable
private fun FormatSettingsScaffold(
  state: FormatSettingsState,
  onBack: () -> Unit,
  onOpenAmounts: () -> Unit,
  onOpenDates: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsFormatGroup,
    description = Strings.settingsFormatDesc,
    highlight = highlight,
    onBack = onBack,
  ) {
    item {
      SubSettingsItem(
        title = Strings.settingsAmounts,
        subtitle = AmountsExample.formattedString(isPrivacyEnabled = false),
        icon = MaterialIcons.Speed125,
        onClick = onOpenAmounts,
      )
    }
    item {
      SubSettingsItem(
        title = Strings.settingsFormatDatesGroup,
        subtitle = DatesExample.formatted(),
        icon = MaterialIcons.CalendarViewWeek,
        onClick = onOpenDates,
      )
    }
    item {
      PreferenceGroup(title = Strings.settingsFormatDistanceGroup) {
        ListPreferenceItem(
          preference = state.distanceUnit,
          optionString = { u -> u.label() },
          optionSuffix = null,
          title = Strings.settingsFormatDistance,
          subtitle = null,
          icon = AktualIcons.Location,
        )
      }
    }
  }
}

private val AmountsExample = Amount(1_234.56)

// Day and month both fit in either position, so the order shows
private val DatesExample = LocalDate(year = 2026, month = 2, day = 3)

@Composable
private fun DistanceUnit.label(): String =
  when (this) {
    Metric -> Strings.settingsFormatDistanceMetric
    Imperial -> Strings.settingsFormatDistanceImperial
  }

@PortraitPreview
@Composable
private fun PreviewFormatSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    FormatSettingsScaffold(
      onBack = {},
      onOpenAmounts = {},
      onOpenDates = {},
      state = FormatSettingsState(distanceUnit = ListPreference(Default)),
    )
  }
