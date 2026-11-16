package aktual.prefs.ui.format

import aktual.budget.model.Amount
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.Location
import aktual.core.icons.material.CalendarViewWeek
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Speed125
import aktual.core.l10n.Strings
import aktual.core.model.DistanceUnit
import aktual.core.nav.AmountSettingsNavigator
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
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

@Composable
fun FormatSettingsScreen(
  back: BackNavigator,
  toAmountSettings: AmountSettingsNavigator,
  highlight: SettingsHighlight?,
  viewModel: FormatSettingsViewModel = metroViewModel<FormatSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  FormatSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = { back() },
    onOpenAmounts = { toAmountSettings() },
  )
}

@Composable
private fun FormatSettingsScaffold(
  state: FormatSettingsState,
  onBack: () -> Unit,
  onOpenAmounts: () -> Unit,
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
      PreferenceGroup(title = Strings.settingsFormatDatesGroup) {
        ListPreferenceItem(
          preference = state.dateFormat,
          optionString = { f -> f.label() },
          optionSuffix = null,
          title = Strings.settingsFormatDates,
          subtitle = null,
          icon = MaterialIcons.CalendarViewWeek,
        )
        ListPreferenceItem(
          preference = state.firstDayOfWeek,
          optionString = { d -> d.string() },
          optionSuffix = null,
          title = Strings.settingsFormatFirstDay,
          subtitle = null,
          icon = AktualIcons.Calendar3,
        )
      }
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

@Composable
private fun DateFormat.label(): String =
  when (this) {
    MmDdYyyy -> Strings.settingsDateFormatMmDdYyyy
    DdMmYyyy -> Strings.settingsDateFormatDdMmYyyy
    YyyyMmDd -> Strings.settingsDateFormatYyyyMmDd
    MmDdYyyyDot -> Strings.settingsDateFormatMmDdYyyyDot
    DdMmYyyyDot -> Strings.settingsDateFormatDdMmYyyyDot
    DdMmYyyyDash -> Strings.settingsDateFormatDdMmYyyyDash
  }

@Composable
private fun DistanceUnit.label(): String =
  when (this) {
    Metric -> Strings.settingsFormatDistanceMetric
    Imperial -> Strings.settingsFormatDistanceImperial
  }

@Composable
private fun FirstDayOfWeek.string(): String =
  when (this) {
    Sunday -> Strings.weekSunday
    Monday -> Strings.weekMonday
    Tuesday -> Strings.weekTuesday
    Wednesday -> Strings.weekWednesday
    Thursday -> Strings.weekThursday
    Friday -> Strings.weekFriday
    Saturday -> Strings.weekSaturday
  }

@PortraitPreview
@Composable
private fun PreviewFormatSettingsScaffold(
  @PreviewParameter(FormatSettingsProvider::class) params: ColorParams<FormatSettingsParams>,
) =
  PreviewWithColorParams(params) {
    FormatSettingsScaffold(
      onBack = {},
      onOpenAmounts = {},
      state =
        FormatSettingsState(
          dateFormat = ListPreference(dateFormat),
          firstDayOfWeek = ListPreference(firstDayOfWeek),
          distanceUnit = ListPreference(Default),
        ),
    )
  }

private data class FormatSettingsParams(
  val dateFormat: DateFormat,
  val firstDayOfWeek: FirstDayOfWeek,
)

private class FormatSettingsProvider :
  ColorParamsProvider<FormatSettingsParams>(
    FormatSettingsParams(
      dateFormat = MmDdYyyy,
      firstDayOfWeek = Monday,
    ),
  )
