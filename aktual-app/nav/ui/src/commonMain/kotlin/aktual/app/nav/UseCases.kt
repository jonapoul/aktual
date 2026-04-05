@file:Suppress("LongParameterList")

package aktual.app.nav

import aktual.api.client.TokenExpiredEvent
import aktual.budget.di.BudgetGraphHolder
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DateFormat
import aktual.budget.model.DbMetadata
import aktual.budget.model.NumberFormat
import aktual.budget.model.database
import aktual.budget.model.metadata
import aktual.budget.model.readMetadata
import aktual.core.connection.ConnectionMonitor
import aktual.core.connection.ServerPinger
import aktual.core.connection.ServerVersionFetcher
import aktual.core.model.PingStateHolder
import aktual.core.model.Token
import aktual.core.ui.BlurConfig
import aktual.core.ui.BottomBarState
import aktual.prefs.AppPreferences
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import aktual.prefs.Preference
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import aktual.prefs.delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import app.cash.molecule.launchMolecule
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import logcat.logcat

@Inject
class BlurConfigUseCase(private val preferences: SystemUiPreferences) {
  operator fun invoke(scope: CoroutineScope): StateFlow<BlurConfig> =
    scope.launchMolecule(Immediate) {
      val blurAppBars by preferences.blurAppBars.collectAsStateFlow(scope)
      val blurDialogs by preferences.blurDialogs.collectAsStateFlow(scope)
      val blurRadius by preferences.blurRadius.collectAsStateFlow(scope)
      val blurAlpha by preferences.blurAlpha.collectAsStateFlow(scope)
      BlurConfig(
        blurAppBars = blurAppBars,
        blurDialogs = blurDialogs,
        blurRadius = blurRadius.dp,
        blurAlpha = blurAlpha,
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
class InitialRouteUseCase(
  private val preferences: AppPreferences,
  private val files: BudgetFiles,
  private val budgetGraphHolder: BudgetGraphHolder,
) {
  operator fun invoke(scope: CoroutineScope): StateFlow<NavKey?> {
    val prefValues =
      combine(
        preferences.serverUrl.asFlow(),
        preferences.token.asFlow(),
        preferences.lastOpenedBudgetId.asFlow(),
        ::Triple,
      )
    return scope.launchMolecule(Immediate) {
      val prefs by prefValues.collectAsState(null)
      val (url, token, budgetId) = prefs ?: return@launchMolecule null // still loading
      when {
        url == null -> ServerUrlNavRoute
        token != null -> resolveAuthenticatedRoute(token, budgetId)
        else -> LoginNavRoute
      }
    }
  }

  @Composable
  @Suppress("BracesOnWhenStatements")
  private fun resolveAuthenticatedRoute(token: Token, budgetId: BudgetId?): NavKey? =
    when {
      budgetId == null -> ListBudgetsNavRoute(token)

      !localFilesExist(budgetId) -> ListBudgetsNavRoute(token)

      else -> {
        var graphReady by
          remember(budgetId) {
            // If the graph is already loaded for this budget, skip re-initialization
            mutableStateOf(budgetGraphHolder.value?.budgetId == budgetId)
          }
        var failed by remember(budgetId) { mutableStateOf(false) }
        if (!graphReady) {
          LaunchedEffect(budgetId) {
            try {
              val metadata = files.readMetadata(budgetId)
              budgetGraphHolder.update(metadata)
              graphReady = true
            } catch (e: Exception) {
              logcat.w(e) {
                "Failed to load budget graph for $budgetId, falling back to budget list"
              }
              failed = true
            }
          }
        }
        when {
          failed -> ListBudgetsNavRoute(token)
          graphReady -> BudgetNavRailNavRoute(token, budgetId)
          else -> null // still loading budget graph
        }
      }
    }

  private fun localFilesExist(id: BudgetId): Boolean =
    with(files) {
      listOf(database(id, mkdirs = false), metadata(id, mkdirs = false)).all(fileSystem::exists)
    }
}

@Inject
class BottomBarStateUseCase(
  private val preferences: SystemUiPreferences,
  private val budgetGraphHolder: BudgetGraphHolder,
  private val pingStateHolder: PingStateHolder,
) {
  operator fun invoke(scope: CoroutineScope): StateFlow<BottomBarState> {
    val showStatusBar = preferences.showBottomBar.asStateFlow(scope)
    val budgetGraph = budgetGraphHolder.stateIn(scope, Eagerly, initialValue = null)
    val budgetName: Flow<String?> = budgetGraph.flatMapLatest { bg ->
      bg?.localPreferences?.map { meta -> meta[DbMetadata.BudgetName] } ?: flowOf(null)
    }

    return scope.launchMolecule(Immediate) {
      val showStatusBar by showStatusBar.collectAsState()
      val budgetName by budgetName.collectAsState(initial = null)
      val pingState by pingStateHolder.collectAsState()
      if (showStatusBar) {
        BottomBarState.Visible(pingState = pingState, budgetName = budgetName)
      } else {
        BottomBarState.Hidden
      }
    }
  }
}

@Inject
class AppLifecycleManager(
  private val connectionMonitor: ConnectionMonitor,
  private val serverPinger: ServerPinger,
  private val serverVersionFetcher: ServerVersionFetcher,
  private val preferences: AppPreferences,
  private val files: BudgetFiles,
  tokenExpiredEvent: TokenExpiredEvent,
) {
  val tokenExpired: Flow<Unit> = tokenExpiredEvent.event

  fun start(scope: CoroutineScope) {
    logcat.v { "start" }
    serverPinger.start()
    connectionMonitor.start()
    scope.launch { serverVersionFetcher.start() }
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
    serverPinger.stop()
    connectionMonitor.stop()

    with(files) {
      val tmpDir = tmp(mkdirs = false)
      fileSystem.deleteRecursively(tmpDir)
    }
  }
}

@Composable
private fun <T : Any> Preference<T>.collectAsStateFlow(scope: CoroutineScope): State<T> {
  val stateFlow = remember(this) { asStateFlow(scope) }
  return stateFlow.collectAsState()
}
