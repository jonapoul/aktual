package aktual.prefs.ui.root

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
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.PreferenceGroup
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.root.FormatConfigState
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

@Preview
@Composable
private fun PreviewFormattingGroup(
  @PreviewParameter(FormattingGroupProvider::class) params: ColoredParams<FormattingGroupState>
) =
  PreviewWithColors(params.colors) {
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
  ColoredParameterProvider<FormattingGroupState>(
    FormattingGroupState(
      numberFormat = CommaDot,
      hideFraction = true,
      dateFormat = MmDdYyyy,
      firstDayOfWeek = Monday,
    )
  )
