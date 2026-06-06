package aktual.prefs.ui.root

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormat
import aktual.core.l10n.Strings
import aktual.core.nav.BackNavigator
import aktual.core.nav.ThemeSettingsNavigator
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameters
import aktual.core.ui.Dimens
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.PageBackground
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.blurredTopBar
import aktual.core.ui.blurredTopBarContent
import aktual.core.ui.blurredTopBarContentPadding
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import aktual.prefs.vm.BooleanPreference
import aktual.prefs.vm.ListPreference
import aktual.prefs.vm.root.BlurAlphaPreference
import aktual.prefs.vm.root.BlurRadiusPreference
import aktual.prefs.vm.root.CurrencyConfigState
import aktual.prefs.vm.root.FormatConfigState
import aktual.prefs.vm.root.SettingsScreenState
import aktual.prefs.vm.root.SettingsViewModel
import aktual.prefs.vm.root.SystemUiConfigState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel

@Composable
fun SettingsScreen(
  back: BackNavigator,
  toThemeSettings: ThemeSettingsNavigator,
  viewModel: SettingsViewModel = metroViewModel<SettingsViewModel>(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  SettingsScaffold(
    state = state,
    onAction = { action ->
      when (action) {
        NavBack -> back()
        NavToThemeSettings -> toThemeSettings()
      }
    },
  )
}

@Composable
private fun SettingsScaffold(state: SettingsScreenState, onAction: SettingsActionHandler) {
  val listState = rememberLazyListState()
  val blurState = rememberBlurredTopBarState()

  Scaffold(
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = colors.transparentTopAppBarColors(),
        navigationIcon = { NavBackIconButton { onAction(NavBack) } },
        title = { Text(Strings.settingsToolbar) },
      )
    }
  ) { innerPadding ->
    Box {
      PageBackground()
      SettingsContent(
        modifier = Modifier.blurredTopBarContent(blurState, innerPadding),
        contentPadding = blurredTopBarContentPadding(blurState, innerPadding),
        listState = listState,
        state = state,
        onAction = onAction,
      )
    }
  }
}

@Composable
private fun SettingsContent(
  state: SettingsScreenState,
  contentPadding: PaddingValues,
  listState: LazyListState,
  onAction: SettingsActionHandler,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().scrollbar(listState).padding(Dimens.Large),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item { ThemeSettingsItem(onClick = { onAction(NavToThemeSettings) }) }
    item { SystemUiGroup(state.systemUi) }
    item { FormattingGroup(state.format) }
    item { CurrencyGroup(state.currency) }
    item { BottomSpacing() }
  }
}

@PortraitPreview
@Composable
private fun PreviewSettingsScaffold(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    SettingsScaffold(
      onAction = {},
      state =
        SettingsScreenState(
          systemUi =
            SystemUiConfigState(
              showStatusBar = BooleanPreference(true),
              blurAppBars = BooleanPreference(true),
              blurDialogs = BooleanPreference(true),
              blurRadiusDp = BlurRadiusPreference(5f),
              blurAlpha = BlurAlphaPreference(0.5f),
            ),
          format =
            FormatConfigState(
              numberFormat = ListPreference(NumberFormat.CommaDot),
              dateFormat = ListPreference(DateFormat.MmDdYyyy),
              firstDayOfWeek = ListPreference(FirstDayOfWeek.Monday),
              hideFraction = BooleanPreference(true),
            ),
          currency =
            CurrencyConfigState(
              currency = ListPreference(Currency.PoundSterling),
              symbolPosition = ListPreference(CurrencySymbolPosition.BeforeAmount),
              spaceBetweenAmountAndSymbol = BooleanPreference(true),
            ),
        ),
    )
  }
