package aktual.settings.ui.root

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
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
import aktual.settings.ui.BooleanPreferenceItem
import aktual.settings.ui.ListPreferenceItem
import aktual.settings.ui.PreferenceGroup
import aktual.settings.vm.BooleanPreference
import aktual.settings.vm.ListPreference
import aktual.settings.vm.root.FormatConfigState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
internal fun FormattingGroup(state: FormatConfigState, modifier: Modifier = Modifier) {
  PreferenceGroup(
    title = Strings.settingsFormatGroup,
    subtitle = Strings.settingsFormatDesc,
    modifier = modifier,
  ) {
    ListPreferenceItem(
      preference = state.numberFormat,
      optionString = { f -> f.string() },
      optionSuffix = null,
      title = Strings.settingsFormatNumbers,
      subtitle = null,
      includeBackground = false,
      icon = MaterialIcons.Speed125,
    )

    ListPreferenceItem(
      preference = state.dateFormat,
      optionString = { f -> f.label() },
      optionSuffix = null,
      title = Strings.settingsFormatDates,
      subtitle = null,
      includeBackground = false,
      icon = MaterialIcons.CalendarViewWeek,
    )

    ListPreferenceItem(
      preference = state.firstDayOfWeek,
      optionString = { d -> d.string() },
      optionSuffix = null,
      title = Strings.settingsFormatFirstDay,
      subtitle = null,
      includeBackground = false,
      icon = MaterialIcons.CalendarToday,
    )

    BooleanPreferenceItem(
      preference = state.hideFraction,
      title = Strings.settingsFormatDecimal,
      subtitle = null,
      includeBackground = false,
      icon =
        if (state.hideFraction.value) {
          MaterialIcons.DecimalDecrease
        } else {
          MaterialIcons.DecimalIncrease
        },
    )
  }
}

@Stable
private fun NumberFormat.string(): String =
  when (this) {
    NumberFormat.CommaDot -> "123,456.78"
    NumberFormat.DotComma -> "123.456,78"
    NumberFormat.SpaceComma -> "123 456,78"
    NumberFormat.ApostropheDot -> "123'456.78"
    NumberFormat.CommaDotIn -> "1,23,456.78"
  }

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

@Preview
@Composable
private fun PreviewFormattingGroup(
  @PreviewParameter(FormattingGroupProvider::class) params: ThemedParams<FormattingGroupState>
) =
  PreviewWithColorScheme(params.theme) {
    FormattingGroup(
      FormatConfigState(
        numberFormat = ListPreference(params.data.numberFormat),
        dateFormat = ListPreference(params.data.dateFormat),
        firstDayOfWeek = ListPreference(params.data.firstDayOfWeek),
        hideFraction = BooleanPreference(params.data.hideFraction),
      )
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
