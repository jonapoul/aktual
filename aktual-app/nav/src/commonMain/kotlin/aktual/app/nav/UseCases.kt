@file:Suppress("LongParameterList")

package aktual.app.nav

import aktual.budget.BudgetFiles
import aktual.budget.SyncStateHolder
import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.DbMetadata
import aktual.budget.model.NumberFormat
import aktual.core.TokenExpiredEvent
import aktual.core.model.PingStateHolder
import aktual.core.nav.BudgetNavRailNavRoute
import aktual.core.nav.ListBudgetsNavRoute
import aktual.core.nav.LoginNavRoute
import aktual.core.nav.ServerUrlNavRoute
import aktual.core.ui.BottomBarState
import aktual.core.ui.HazeConfig
import aktual.di.AppGraph
import aktual.di.BudgetGraph
import aktual.di.LoggedInGraph
import aktual.di.RunLevelState
import aktual.di.ServerChosenGraph
import aktual.prefs.AppPreferences
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.logcat

@Inject
class HazeConfigUseCase(private val preferences: SystemUiPreferences) {
  operator fun invoke(scope: CoroutineScope): StateFlow<HazeConfig> =
    scope.launchMolecule(Immediate) {
      val hazeAppBars by preferences.hazeAppBars.collectAsStateFlow(scope)
      val hazeDialogs by preferences.hazeDialogs.collectAsStateFlow(scope)
      val hazeRadius by preferences.hazeRadius.collectAsStateFlow(scope)
      val hazeAlpha by preferences.hazeAlpha.collectAsStateFlow(scope)
      HazeConfig(
        appBars = hazeAppBars,
        dialogs = hazeDialogs,
        radius = hazeRadius.dp,
        alpha = hazeAlpha,
      )
    }
}

@Immutable
data class FormatConfig(
  val dateFormat: DateFormat,
  val numberFormat: NumberFormat,
  val hideFraction: Boolean,
  val currency: Currency,
  val symbolPosition: CurrencySymbolPosition,
  val spaceBetweenAmountAndSymbol: Boolean,
  val isPrivacyEnabled: Boolean,
)

@Inject
class FormatConfigUseCase(
  private val prefs: AppPreferences,
  private val formats: FormatPreferences,
  private val currencies: CurrencyPreferences,
) {
  operator fun invoke(scope: CoroutineScope): StateFlow<FormatConfig> =
    scope.launchMolecule(Immediate) {
      val dateFormat by formats.dateFormat.collectAsStateFlow(scope)
      val numberFormat by formats.numberFormat.collectAsStateFlow(scope)
      val hideFraction by formats.hideFraction.collectAsStateFlow(scope)
      val currency by currencies.currency.collectAsStateFlow(scope)
      val currencySymbolPosition by currencies.symbolPosition.collectAsStateFlow(scope)
      val currencySpaceBetweenAmountAndSymbol by
        currencies.spaceBetweenAmountAndSymbol.collectAsStateFlow(scope)
      val isPrivacyEnabled by prefs.isPrivacyEnabled.collectAsStateFlow(scope)
      FormatConfig(
        dateFormat = dateFormat,
        numberFormat = numberFormat,
        hideFraction = hideFraction,
        currency = currency,
        symbolPosition = currencySymbolPosition,
        spaceBetweenAmountAndSymbol = currencySpaceBetweenAmountAndSymbol,
        isPrivacyEnabled = isPrivacyEnabled,
      )
    }
}

@Inject
class InitialRouteUseCase(private val runLevelState: RunLevelState) {
  operator fun invoke(scope: CoroutineScope): StateFlow<NavKey?> =
    runLevelState
      .all()
      .map { graphs ->
        when (graphs.lastOrNull()) {
          null -> null // not initialised yet?
          is AppGraph -> ServerUrlNavRoute
          is ServerChosenGraph -> LoginNavRoute
          is LoggedInGraph -> ListBudgetsNavRoute
          is BudgetGraph -> BudgetNavRailNavRoute
        }
      }
      .stateIn(scope = scope, started = Eagerly, initialValue = null)
}

@Inject
class BottomBarStateUseCase(
  private val preferences: SystemUiPreferences,
  private val runLevelState: RunLevelState,
  private val pingStateHolder: PingStateHolder,
  private val syncStateHolder: SyncStateHolder,
) {
  operator fun invoke(scope: CoroutineScope): StateFlow<BottomBarState> {
    val showStatusBar = preferences.showBottomBar.asStateFlow(scope)

    val budgetName: Flow<String?> =
      runLevelState.budget().flatMapLatest { bg ->
        bg?.localPreferences?.map { meta -> meta[DbMetadata.BudgetName] } ?: flowOf(null)
      }

    return scope.launchMolecule(Immediate) {
      val showStatusBar by showStatusBar.collectAsState()
      val budgetName by budgetName.collectAsState(initial = null)
      val pingState by pingStateHolder.collectAsState()
      val syncState by syncStateHolder.collectAsState()
      if (showStatusBar) {
        BottomBarState.Visible(pingState, syncState, budgetName)
      } else {
        BottomBarState.Hidden
      }
    }
  }
}

@Inject
class AppLifecycleManager(
  private val preferences: AppPreferences,
  private val files: BudgetFiles,
  tokenExpiredEvent: TokenExpiredEvent,
) {
  val tokenExpired: Flow<Unit> = tokenExpiredEvent.event

  fun start(scope: CoroutineScope) {
    logcat.v { "start" }
    scope.launch {
      tokenExpired.collect {
        logcat.w { "Token expired, clearing stored token" }
        preferences.token.delete()
        preferences.lastOpenedBudgetId.delete()
      }
    }
  }

  fun destroy() {
    logcat.v { "destroy" }
    files.close()
  }
}

@Composable
private fun <T : Any> Preference<T>.collectAsStateFlow(scope: CoroutineScope): State<T> {
  val stateFlow = remember(this) { asStateFlow(scope) }
  return stateFlow.collectAsState()
}
