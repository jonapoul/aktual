package aktual.settings.ui.root

import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.icons.CalendarToday
import aktual.core.icons.CalendarViewWeek
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
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun FormattingGroup(
  numberFormat: NumberFormatPreference,
  hideFraction: BooleanPreference,
  dateFormat: DateFormat,
  firstDayOfWeek: FirstDayOfWeek,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  PreferenceGroup(
    title = Strings.settingsFormatGroup,
    subtitle = Strings.settingsFormatDesc,
    modifier = modifier,
  ) {
    NumberFormat(numberFormat, onAction)
    DateFormatItem(dateFormat, onAction)
    FirstDayOfWeek(firstDayOfWeek, onAction)
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
private fun FirstDayOfWeek(
  value: FirstDayOfWeek,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  ListPreferenceItem(
    modifier = modifier,
    value = value,
    options = FirstDayOfWeekValues,
    optionString = { d -> d.string() },
    optionIcon = null,
    onValueChange = { d -> onAction(SettingsAction.SetFirstDayOfWeek(d)) },
    title = Strings.settingsFormatFirstDay,
    subtitle = null,
    includeBackground = false,
    icon = MaterialIcons.CalendarToday,
    enabled = true,
  )
}

private val FirstDayOfWeekValues = FirstDayOfWeek.entries.toImmutableList()

@Composable
private fun DateFormatItem(
  value: DateFormat,
  onAction: (SettingsAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  ListPreferenceItem(
    modifier = modifier,
    value = value,
    options = DateFormatValues,
    optionString = { f -> f.label() },
    optionIcon = null,
    onValueChange = { f -> onAction(SettingsAction.SetDateFormat(f)) },
    title = Strings.settingsFormatDates,
    subtitle = null,
    includeBackground = false,
    icon = MaterialIcons.CalendarViewWeek,
    enabled = true,
  )
}

private val DateFormatValues = DateFormat.entries.toImmutableList()

@Composable
private fun DateFormat.label(): String =
  when (this) {
    DateFormat.MmDdYyyy -> Strings.settingsDateFormatMmDdYyyy
    DateFormat.DdMmYyyy -> Strings.settingsDateFormatDdMmYyyy
    DateFormat.YyyyMmDd -> Strings.settingsDateFormatYyyyMmDd
    DateFormat.MmDdYyyyDot -> Strings.settingsDateFormatMmDdYyyyDot
    DateFormat.DdMmYyyyDot -> Strings.settingsDateFormatDdMmYyyyDot
  }

@Composable
private fun FirstDayOfWeek.string(): String =
  when (this) {
    FirstDayOfWeek.Sunday -> Strings.weekSunday
    FirstDayOfWeek.Monday -> Strings.weekMonday
    FirstDayOfWeek.Tuesday -> Strings.weekTuesday
    FirstDayOfWeek.Wednesday -> Strings.weekWednesday
    FirstDayOfWeek.Thursday -> Strings.weekThursday
    FirstDayOfWeek.Friday -> Strings.weekFriday
    FirstDayOfWeek.Saturday -> Strings.weekSaturday
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
      dateFormat = params.data.dateFormat,
      firstDayOfWeek = params.data.firstDayOfWeek,
      onAction = {},
    )
  }

private data class FormattingGroupState(
  val numberFormat: NumberFormat,
  val hideFraction: Boolean,
  val dateFormat: DateFormat,
  val firstDayOfWeek: FirstDayOfWeek,
)

private class FormattingGroupProvider :
  ThemedParameterProvider<FormattingGroupState>(
    FormattingGroupState(
      numberFormat = NumberFormat.CommaDot,
      hideFraction = true,
      dateFormat = DateFormat.MmDdYyyy,
      firstDayOfWeek = FirstDayOfWeek.Monday,
    )
  )
