package aktual.prefs.ui.format

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.icons.material.CalendarToday
import aktual.core.icons.material.CalendarViewWeek
import aktual.core.icons.material.DecimalDecrease
import aktual.core.icons.material.DecimalIncrease
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Speed125
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColoredParams
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.format.FormatSettingsState
import aktual.prefs.vm.format.FormatSettingsViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun FormatSettingsScreen(
  back: BackNavigator,
  viewModel: FormatSettingsViewModel = metroViewModel<FormatSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  FormatSettingsScaffold(state = state, onBack = { back() })
}

@Composable
private fun FormatSettingsScaffold(state: FormatSettingsState, onBack: () -> Unit) {
  SettingsListScaffold(
    title = Strings.settingsFormatGroup,
    description = Strings.settingsFormatDesc,
    onBack = onBack,
  ) {
    item {
      ListPreferenceItem(
        preference = state.numberFormat,
        optionString = { f -> f.string() },
        optionSuffix = null,
        title = Strings.settingsFormatNumbers,
        subtitle = null,
        icon = MaterialIcons.Speed125,
      )
    }
    item {
      ListPreferenceItem(
        preference = state.dateFormat,
        optionString = { f -> f.label() },
        optionSuffix = null,
        title = Strings.settingsFormatDates,
        subtitle = null,
        icon = MaterialIcons.CalendarViewWeek,
      )
    }
    item {
      ListPreferenceItem(
        preference = state.firstDayOfWeek,
        optionString = { d -> d.string() },
        optionSuffix = null,
        title = Strings.settingsFormatFirstDay,
        subtitle = null,
        icon = MaterialIcons.CalendarToday,
      )
    }
    item {
      BooleanPreferenceItem(
        preference = state.hideFraction,
        title = Strings.settingsFormatDecimal,
        subtitle = null,
        icon =
          if (state.hideFraction.value) {
            MaterialIcons.DecimalDecrease
          } else {
            MaterialIcons.DecimalIncrease
          },
      )
    }
  }
}

@Stable
private fun NumberFormat.string(): String =
  when (this) {
    CommaDot -> "123,456.78"
    DotComma -> "123.456,78"
    SpaceComma -> "123 456,78"
    ApostropheDot -> "123'456.78"
    CommaDotIn -> "1,23,456.78"
  }

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
  @PreviewParameter(FormatSettingsProvider::class) params: ColoredParams<FormatSettingsParams>,
) =
  PreviewWithColoredParams(params) {
    FormatSettingsScaffold(
      onBack = {},
      state =
        FormatSettingsState(
          numberFormat = ListPreference(numberFormat),
          dateFormat = ListPreference(dateFormat),
          firstDayOfWeek = ListPreference(firstDayOfWeek),
          hideFraction = BooleanPreference(hideFraction),
        ),
    )
  }

private data class FormatSettingsParams(
  val numberFormat: NumberFormat,
  val hideFraction: Boolean,
  val dateFormat: DateFormat,
  val firstDayOfWeek: FirstDayOfWeek,
)

private class FormatSettingsProvider :
  ColoredParameterProvider<FormatSettingsParams>(
    FormatSettingsParams(
      numberFormat = CommaDot,
      hideFraction = true,
      dateFormat = MmDdYyyy,
      firstDayOfWeek = Monday,
    ),
  )
