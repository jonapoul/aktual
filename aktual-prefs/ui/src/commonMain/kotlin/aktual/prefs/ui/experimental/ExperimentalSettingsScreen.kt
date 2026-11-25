package aktual.prefs.ui.experimental

import aktual.budget.model.SyncedPrefKey.FeatureFlag
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.SettingsHighlight
import aktual.core.ui.ColorParameters
import aktual.core.ui.NoticeBanner
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.WarningBanner
import aktual.prefs.components.PreferenceGroup
import aktual.prefs.ui.BooleanPreferenceItem
import aktual.prefs.ui.SettingsListScaffold
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.experimental.ExperimentalFeature
import aktual.prefs.vm.experimental.ExperimentalSettingsState
import aktual.prefs.vm.experimental.ExperimentalSettingsViewModel
import aktual.theme.Colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ExperimentalSettingsScreen(
  back: BackNavigator,
  highlight: SettingsHighlight?,
  viewModel: ExperimentalSettingsViewModel = metroViewModel<ExperimentalSettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val uriHandler = LocalUriHandler.current
  ExperimentalSettingsScaffold(
    state = state,
    highlight = highlight,
    onBack = back::invoke,
    onOpenDocs = { uriHandler.openUri(DOCS_URL) },
  )
}

@Composable
private fun ExperimentalSettingsScaffold(
  state: ExperimentalSettingsState,
  onBack: () -> Unit,
  onOpenDocs: () -> Unit,
  highlight: SettingsHighlight? = null,
) {
  SettingsListScaffold(
    title = Strings.settingsExperimental,
    description = null,
    onBack = onBack,
    highlight = highlight,
  ) {
    item { WarningBanner(text = Strings.settingsExperimentalDesc) }

    item { NoticeBanner(text = Strings.settingsExperimentalDocs, onClick = onOpenDocs) }

    item {
      PreferenceGroup(
        title = Strings.settingsExperimentalSynced,
        subtitle = Strings.settingsExperimentalSyncedDesc,
      ) {
        state.features.fastForEach { feature ->
          BooleanPreferenceItem(
            preference = feature.preference,
            title = feature.flag.title(),
            subtitle = if (feature.isSupported) null else Strings.settingsExperimentalUnsupported,
            icon = null,
          )
        }
      }
    }
  }
}

@Composable
private fun FeatureFlag.title(): String =
  when (this) {
    GoalTemplates -> Strings.settingsExperimentalGoalTemplates
    GoalTemplatesUi -> Strings.settingsExperimentalGoalTemplatesUi
    ActionTemplating -> Strings.settingsExperimentalActionTemplating
    FormulaMode -> Strings.settingsExperimentalFormulaMode
    CurrencySupport -> Strings.settingsExperimentalCurrency
    MobileCalculator -> Strings.settingsExperimentalMobileCalculator
    NewSidebarUi -> Strings.settingsExperimentalNewSidebar
    SankeyReport -> Strings.settingsExperimentalSankeyReport
    BalanceForecastReport -> Strings.settingsExperimentalBalanceForecastReport
    BudgetAnalysisReport -> Strings.settingsExperimentalBudgetAnalysisReport
    MonteCarloReport -> Strings.settingsExperimentalMonteCarloReport
  }

@PortraitPreview
@Composable
private fun PreviewExperimentalSettingsScaffold(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    ExperimentalSettingsScaffold(
      onBack = {},
      onOpenDocs = {},
      state =
        ExperimentalSettingsState(
          features =
            FeatureFlag.entries
              .mapIndexed { i, flag ->
                val isSupported = i % 2 == 0
                ExperimentalFeature(
                  flag = flag,
                  preference = BooleanPreference(value = i % 3 == 0, enabled = isSupported),
                  isSupported = isSupported,
                )
              }
              .toImmutableList(),
        ),
    )
  }

private const val DOCS_URL = "https://actualbudget.org/docs/experimental/"
