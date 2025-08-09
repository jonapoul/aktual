@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.app.android

import actual.account.model.LoginToken
import actual.budget.db.dao.PreferencesDao
import actual.budget.di.BudgetGraphHolder
import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
import actual.budget.model.NumberFormat
import actual.budget.model.SyncedPrefKey
import actual.core.connection.ConnectionMonitor
import actual.core.connection.ServerPinger
import actual.core.connection.ServerVersionFetcher
import actual.core.di.ViewModelKey
import actual.core.di.ViewModelScope
import actual.core.model.DarkColorSchemeType
import actual.core.model.PingStateHolder
import actual.core.model.RegularColorSchemeType
import actual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.jonpoulton.preferences.core.asStateFlow
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import logcat.logcat

@Inject
@ViewModelKey(ActualActivityViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class ActualActivityViewModel(
  private val appScope: CoroutineScope,
  private val contexts: CoroutineContexts,
  private val connectionMonitor: ConnectionMonitor,
  private val serverPinger: ServerPinger,
  private val pingStateHolder: PingStateHolder,
  private val serverVersionFetcher: ServerVersionFetcher,
  private val files: BudgetFiles,
  budgetComponents: BudgetGraphHolder,
  preferences: AppGlobalPreferences,
) : ViewModel() {
  private val component = budgetComponents.stateIn(viewModelScope, Eagerly, initialValue = null)

  private val syncedPrefs: StateFlow<PreferencesDao?> = component
    .filterNotNull()
    .map { c -> PreferencesDao(c.database, contexts) }
    .stateIn(viewModelScope, Eagerly, initialValue = null)

  val numberFormat: StateFlow<NumberFormat> = observeSyncedPref(
    key = SyncedPrefKey.Global.NumberFormat,
    default = NumberFormat.Default,
    mapper = NumberFormat::from,
  )

  val hideFraction: StateFlow<Boolean> = observeSyncedPref(
    key = SyncedPrefKey.Global.HideFraction,
    default = false,
    mapper = { it.toBoolean() },
  )

  val isPrivacyEnabled: StateFlow<Boolean> = observeSyncedPref(
    key = SyncedPrefKey.Global.IsPrivacyEnabled,
    default = false,
    mapper = { it.toBoolean() },
  )

  val regularSchemeType: StateFlow<RegularColorSchemeType> = preferences.regularColorScheme.asStateFlow(viewModelScope)
  val darkSchemeType: StateFlow<DarkColorSchemeType> = preferences.darkColorScheme.asStateFlow(viewModelScope)

  val isServerUrlSet: Boolean = preferences.serverUrl.isSet()
  val loginToken: LoginToken? = preferences.loginToken.get()

  private val showStatusBar = preferences.showBottomBar.asStateFlow(viewModelScope)

  private val budgetName: Flow<String?> = component.flatMapLatest { component ->
    component
      ?.localPreferences
      ?.map { meta -> meta[DbMetadata.BudgetName] }
      ?: flowOf(null)
  }

  val bottomBarState: StateFlow<BottomBarState> = viewModelScope.launchMolecule(Immediate) {
    val showStatusBar by showStatusBar.collectAsState()
    val budgetName by budgetName.collectAsState(initial = null)
    val pingState by pingStateHolder.collectAsState()
    if (showStatusBar) {
      BottomBarState.Visible(
        pingState = pingState,
        budgetName = budgetName,
      )
    } else {
      BottomBarState.Hidden
    }
  }

  fun start() {
    serverPinger.start()
    connectionMonitor.start()
    viewModelScope.launch {
      serverVersionFetcher.startFetching()
    }
  }

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

  private fun <T> observeSyncedPref(key: SyncedPrefKey, default: T, mapper: (String) -> T?): StateFlow<T> = syncedPrefs
    .filterNotNull()
    .flatMapLatest { dao -> dao.observe(key).map { value -> value?.let { mapper(it) } ?: default } }
    .stateIn(viewModelScope, Eagerly, initialValue = default)
}
