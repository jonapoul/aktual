/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
@file:OptIn(ExperimentalCoroutinesApi::class)

package aktual.app.nav

import aktual.budget.db.dao.PreferencesDao
import aktual.budget.model.BudgetFiles
import aktual.budget.model.DbMetadata
import aktual.budget.model.NumberFormat
import aktual.budget.model.SyncedPrefKey
import aktual.core.connection.ConnectionMonitor
import aktual.core.connection.ServerPinger
import aktual.core.connection.ServerVersionFetcher
import aktual.core.di.BudgetGraphHolder
import aktual.core.model.DarkColorSchemeType
import aktual.core.model.LoginToken
import aktual.core.model.PingStateHolder
import aktual.core.model.RegularColorSchemeType
import aktual.core.ui.BottomBarState
import aktual.prefs.AppGlobalPreferences
import alakazam.kotlin.core.CoroutineContexts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dev.jonpoulton.preferences.core.asStateFlow
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

@Suppress("AbstractClassCanBeConcreteClass")
abstract class RootViewModel(
  protected val appScope: CoroutineScope,
  protected val contexts: CoroutineContexts,
  protected val connectionMonitor: ConnectionMonitor,
  protected val serverPinger: ServerPinger,
  protected val pingStateHolder: PingStateHolder,
  protected val serverVersionFetcher: ServerVersionFetcher,
  protected val files: BudgetFiles,
  budgetComponents: BudgetGraphHolder,
  preferences: AppGlobalPreferences,
) : ViewModel() {
  private val budgetGraph = budgetComponents.stateIn(viewModelScope, Eagerly, initialValue = null)

  private val syncedPrefs: StateFlow<PreferencesDao?> = budgetGraph
    .filterNotNull()
    .map { bg -> PreferencesDao(bg.database, contexts) }
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

  private val budgetName: Flow<String?> = budgetGraph.flatMapLatest { bg ->
    bg
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
