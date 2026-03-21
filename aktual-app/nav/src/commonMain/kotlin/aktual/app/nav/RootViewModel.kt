@file:Suppress("AbstractClassCanBeConcreteClass", "LongParameterList")

package aktual.app.nav

import aktual.api.client.TokenExpiredEvent
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.BudgetFiles
import aktual.budget.model.Currency
import aktual.budget.model.CurrencySymbolPosition
import aktual.budget.model.DbMetadata
import aktual.budget.model.NumberFormat
import aktual.budget.model.SyncedPrefKey
import aktual.core.connection.ConnectionMonitor
import aktual.core.connection.ServerPinger
import aktual.core.connection.ServerVersionFetcher
import aktual.core.di.BudgetGraphHolder
import aktual.core.model.PingStateHolder
import aktual.core.model.Token
import aktual.core.prefs.AppGlobalPreferences
import aktual.core.prefs.asStateFlow
import aktual.core.prefs.delete
import aktual.core.theme.Theme
import aktual.core.theme.ThemeResolver
import aktual.core.ui.BottomBarState
import alakazam.kotlin.CoroutineContexts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import logcat.logcat

abstract class RootViewModel(
  protected val appScope: CoroutineScope,
  protected val contexts: CoroutineContexts,
  protected val connectionMonitor: ConnectionMonitor,
  protected val serverPinger: ServerPinger,
  protected val pingStateHolder: PingStateHolder,
  protected val serverVersionFetcher: ServerVersionFetcher,
  protected val files: BudgetFiles,
  budgetComponents: BudgetGraphHolder,
  private val preferences: AppGlobalPreferences,
  private val themeResolver: ThemeResolver,
  private val tokenExpiredEvent: TokenExpiredEvent,
) : ViewModel() {
  private val budgetGraph = budgetComponents.stateIn(viewModelScope, Eagerly, initialValue = null)

  private val syncedPrefs: StateFlow<PreferencesDao?> =
    budgetGraph
      .filterNotNull()
      .map { bg -> PreferencesDao(bg.database, contexts) }
      .stateIn(viewModelScope, Eagerly, initialValue = null)

  val numberFormat: StateFlow<NumberFormat> = preferences.numberFormat.asStateFlow(viewModelScope)

  val hideFraction: StateFlow<Boolean> = preferences.hideFraction.asStateFlow(viewModelScope)

  val currency: StateFlow<Currency> = preferences.currency.asStateFlow(viewModelScope)

  val currencySymbolPosition: StateFlow<CurrencySymbolPosition> =
    preferences.currencySymbolPosition.asStateFlow(viewModelScope)

  val currencySpaceBetweenAmountAndSymbol: StateFlow<Boolean> =
    preferences.currencySpaceBetweenAmountAndSymbol.asStateFlow(viewModelScope)

  val isPrivacyEnabled: StateFlow<Boolean> =
    observeSyncedPref(
      key = SyncedPrefKey.Global.IsPrivacyEnabled,
      default = false,
      mapper = { it.toBoolean() },
    )

  val isServerUrlSet: Boolean = runBlocking { preferences.serverUrl.get() } != null

  val token: Token? = runBlocking { preferences.token.get() }
  private val showStatusBar = preferences.showBottomBar.asStateFlow(viewModelScope)

  private val budgetName: Flow<String?> = budgetGraph.flatMapLatest { bg ->
    bg?.localPreferences?.map { meta -> meta[DbMetadata.BudgetName] } ?: flowOf(null)
  }

  val bottomBarState: StateFlow<BottomBarState> =
    viewModelScope.launchMolecule(Immediate) {
      val showStatusBar by showStatusBar.collectAsState()
      val budgetName by budgetName.collectAsState(initial = null)
      val pingState by pingStateHolder.collectAsState()
      if (showStatusBar) {
        BottomBarState.Visible(pingState = pingState, budgetName = budgetName)
      } else {
        BottomBarState.Hidden
      }
    }

  val tokenExpired: Flow<Unit> = tokenExpiredEvent.event

  init {
    serverPinger.start()
    connectionMonitor.start()
    viewModelScope.launch { serverVersionFetcher.start() }
    viewModelScope.launch {
      tokenExpiredEvent.event.collect {
        logcat.w { "Token expired, clearing stored token" }
        preferences.token.delete()
      }
    }
  }

  fun theme(isSystemInDarkTheme: Boolean): Flow<Theme> =
    themeResolver.activeTheme(isSystemInDarkTheme)

  fun onDestroy() {
    logcat.v { "onDestroy" }
    serverPinger.stop()
    connectionMonitor.stop()
    appScope.cancel()

    with(files) {
      val tmpDir = tmp(mkdirs = false)
      fileSystem.deleteRecursively(tmpDir)
    }
  }

  private fun <T> observeSyncedPref(
    key: SyncedPrefKey,
    default: T,
    mapper: (String) -> T?,
  ): StateFlow<T> =
    syncedPrefs
      .filterNotNull()
      .flatMapLatest { dao ->
        dao.observe(key).map { value -> value?.let { mapper(it) } ?: default }
      }
      .stateIn(viewModelScope, Eagerly, initialValue = default)
}
