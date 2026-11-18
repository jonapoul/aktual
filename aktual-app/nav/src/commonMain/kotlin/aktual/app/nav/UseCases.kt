@file:Suppress("LongParameterList")

package aktual.app.nav

import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.DbMetadataKey.Companion.BudgetName
import aktual.budget.model.NumberFormat
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.SyncStateHolder
import aktual.core.model.DistanceUnit
import aktual.core.model.PingStateHolder
import aktual.core.nav.BudgetNavRailNavRoute
import aktual.core.nav.ListBudgetsNavRoute
import aktual.core.nav.LoginNavRoute
import aktual.core.nav.ServerUrlNavRoute
import aktual.core.ui.BottomBarState
import aktual.core.ui.HazeConfig
import aktual.core.utils.TokenExpiredEvent
import aktual.di.AppGraph
import aktual.di.BudgetGraph
import aktual.di.BudgetScope
import aktual.di.LoggedInGraph
import aktual.di.RunLevelState
import aktual.di.ServerChosenGraph
import aktual.log
import aktual.prefs.AppPreferences
import aktual.prefs.BudgetFormatPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Inject
class HazeConfigUseCase(private val preferences: SystemUiPreferences) {
  operator fun invoke(scope: CoroutineScope): StateFlow<HazeConfig> =
    scope.launchMolecule(Immediate) {
      val appBarEffect by preferences.appBarEffect.collectAsStateFlow(scope)
      val hazeDialogs by preferences.hazeDialogs.collectAsStateFlow(scope)
      val barBlurRadius by preferences.barBlurRadius.collectAsStateFlow(scope)
      val barBlurAlpha by preferences.barBlurAlpha.collectAsStateFlow(scope)
      val dialogBlurRadius by preferences.dialogBlurRadius.collectAsStateFlow(scope)
      val dialogBlurAlpha by preferences.dialogBlurAlpha.collectAsStateFlow(scope)
      val glassStyle by preferences.glassStyle.collectAsStateFlow(scope)
      val glassCornerRadius by preferences.glassCornerRadius.collectAsStateFlow(scope)
      HazeConfig(
        appBarEffect = appBarEffect,
        dialogs = hazeDialogs,
        barBlurRadius = barBlurRadius.dp,
        barBlurAlpha = barBlurAlpha,
        dialogBlurRadius = dialogBlurRadius.dp,
        dialogBlurAlpha = dialogBlurAlpha,
        glassStyle = glassStyle,
        glassCornerRadius = glassCornerRadius.dp,
      )
    }
}

data class FormatConfig(
  val dateFormat: DateFormat,
  val numberFormat: NumberFormat,
  val hideFraction: Boolean,
  val currency: Currency,
  val symbolPosition: CurrencySymbolPosition,
  val spaceBetweenAmountAndSymbol: Boolean,
  val isPrivacyEnabled: Boolean,
  val distanceUnit: DistanceUnit,
)

@Inject
class FormatConfigUseCase(
  private val formats: FormatPreferences,
  private val runLevelState: RunLevelState,
) {
  operator fun invoke(scope: CoroutineScope): StateFlow<FormatConfig> {
    val budgetFormats: Flow<BudgetFormats> =
      runLevelState.budget().flatMapLatest { graph ->
        (graph as? BudgetFormatPreferencesGraph)?.budgetFormatPreferences?.observe()
          ?: flowOf(BudgetFormats())
      }

    return scope.launchMolecule(Immediate) {
      val budget by budgetFormats.collectAsState(initial = BudgetFormats())
      val distanceUnit by formats.distanceUnit.collectAsStateFlow(scope)
      FormatConfig(
        dateFormat = budget.dateFormat,
        numberFormat = budget.numberFormat,
        hideFraction = budget.hideFraction,
        currency = budget.currency,
        symbolPosition = budget.symbolPosition,
        spaceBetweenAmountAndSymbol = budget.spaceBetweenAmountAndSymbol,
        isPrivacyEnabled = budget.isPrivacyEnabled,
        distanceUnit = distanceUnit,
      )
    }
  }

  // Defaults match upstream's when a budget has never set them
  private data class BudgetFormats(
    val dateFormat: DateFormat = Default,
    val numberFormat: NumberFormat = Default,
    val hideFraction: Boolean = false,
    val currency: Currency = Default,
    val symbolPosition: CurrencySymbolPosition = Default,
    val spaceBetweenAmountAndSymbol: Boolean = false,
    val isPrivacyEnabled: Boolean = false,
  )

  private fun BudgetFormatPreferences.observe(): Flow<BudgetFormats> =
    combine(
        numberFormat.asFlow(),
        hideFraction.asFlow(),
        currency.asFlow(),
        symbolPosition.asFlow(),
        spaceBetweenAmountAndSymbol.asFlow(),
      ) { numberFormat, hideFraction, currency, symbolPosition, includeSpace ->
        BudgetFormats(
          numberFormat = numberFormat,
          hideFraction = hideFraction,
          currency = currency,
          symbolPosition = symbolPosition,
          spaceBetweenAmountAndSymbol = includeSpace,
        )
      }
      .combine(dateFormat.asFlow()) { formats, dateFormat -> formats.copy(dateFormat = dateFormat) }
      .combine(isPrivacyEnabled.asFlow()) { formats, enabled ->
        formats.copy(isPrivacyEnabled = enabled)
      }
}

/** Gives code outside the budget graph, like [FormatConfigUseCase], the open budget's formats */
@ContributesTo(BudgetScope::class)
interface BudgetFormatPreferencesGraph {
  val budgetFormatPreferences: BudgetFormatPreferences
}

@Inject
class InitialRouteUseCase(private val runLevelState: RunLevelState) {
  operator fun invoke(scope: CoroutineScope): StateFlow<NavKey?> =
    runLevelState
      .all()
      .map { graphs ->
        when (graphs.lastOrNull()) {
          null -> null
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
        bg?.localPreferences?.map { meta -> meta[BudgetName] } ?: flowOf(null)
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
    log.v { "start" }
    scope.launch {
      tokenExpired.collect {
        log.w { "Token expired, clearing stored token" }
        preferences.token.delete()
        preferences.lastOpenedBudgetId.delete()
      }
    }
  }

  fun destroy() {
    log.v { "destroy" }
    files.close()
  }
}

@Composable
private fun <T : Any> Preference<T>.collectAsStateFlow(scope: CoroutineScope): State<T> {
  val stateFlow = remember(this) { asStateFlow(scope) }
  return stateFlow.collectAsState()
}
