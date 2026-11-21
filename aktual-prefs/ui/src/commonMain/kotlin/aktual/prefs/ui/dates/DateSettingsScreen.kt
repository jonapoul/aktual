package aktual.prefs.ui.dates

import aktual.budget.model.DateFormat
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParameters
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.formatter
import aktual.core.ui.tabular
import aktual.prefs.ui.ListPreferenceItem
import aktual.prefs.ui.PreviewOptionGroup
import aktual.prefs.ui.PreviewSettingsScaffold
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.dates.DateSettingsState
import aktual.prefs.vm.dates.DateSettingsViewModel
import aktual.theme.Colors
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.datetime.LocalDate

@Composable
fun DateSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: DateSettingsViewModel = metroViewModel<DateSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  DateSettingsScaffold(state = state, highlight = highlight, onBack = back::invoke)
}

@Composable
private fun DateSettingsScaffold(
  state: DateSettingsState,
  onBack: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  PreviewSettingsScaffold(
    title = Strings.settingsFormatDatesGroup,
    description =
      Strings.settingsDatesDesc(state.budgetName.ifBlank { Strings.settingsBudgetUnnamed }),
    onBack = onBack,
    preview = {
      DatesPreview(
        today = state.today,
        dateFormat = state.dateFormat.value,
        firstDayOfWeek = state.firstDayOfWeek.value,
      )
    },
    highlight = highlight,
  ) {
    item {
      PreviewOptionGroup(
        title = Strings.settingsFormatDates,
        preference = state.dateFormat,
        label = { it.label() },
        preview = { format -> FormatExample(format, state.today) },
      )
    }
    item {
      ListPreferenceItem(
        preference = state.firstDayOfWeek,
        optionString = { it.label() },
        optionSuffix = null,
        title = Strings.settingsFormatFirstDay,
        subtitle = null,
        icon = AktualIcons.Calendar3,
      )
    }
  }
}

@Composable
private fun FormatExample(format: DateFormat, today: LocalDate) =
  Text(
    modifier = Modifier.width(ExampleWidth),
    text = format.formatter().format(today),
    style = typography.bodyMedium.tabular(),
  )

private val ExampleWidth = 96.dp

@PortraitPreview
@Composable
private fun PreviewDateSettingsScaffold(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    DateSettingsScaffold(
      onBack = {},
      state =
        DateSettingsState(
          budgetName = "My Budget",
          dateFormat = ListPreference(DdMmYyyy),
          firstDayOfWeek = ListPreference(Monday),
          today = LocalDate(year = 2026, month = 11, day = 16),
        ),
    )
  }
