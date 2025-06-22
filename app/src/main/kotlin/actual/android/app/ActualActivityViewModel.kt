@file:OptIn(ExperimentalCoroutinesApi::class)

package actual.android.app

import actual.account.model.LoginToken
import actual.api.client.ActualApisStateHolder
import actual.budget.di.BudgetComponentStateHolder
import actual.budget.model.BudgetFiles
import actual.budget.model.DbMetadata
import actual.core.connection.ConnectionMonitor
import actual.core.connection.ServerVersionFetcher
import actual.core.model.DarkColorSchemeType
import actual.core.model.RegularColorSchemeType
import actual.prefs.AppGlobalPreferences
import alakazam.kotlin.logging.Logger
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.molecule.RecompositionMode.Immediate
import app.cash.molecule.launchMolecule
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jonpoulton.preferences.core.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ActualActivityViewModel @Inject constructor(
  private val scope: CoroutineScope,
  private val connectionMonitor: ConnectionMonitor,
  private val serverVersionFetcher: ServerVersionFetcher,
  private val apiStateHolder: ActualApisStateHolder,
  private val files: BudgetFiles,
  budgetComponents: BudgetComponentStateHolder,
  preferences: AppGlobalPreferences,
) : ViewModel() {
  val regularSchemeType: StateFlow<RegularColorSchemeType> = preferences.regularColorScheme.asStateFlow(viewModelScope)
  val darkSchemeType: StateFlow<DarkColorSchemeType> = preferences.darkColorScheme.asStateFlow(viewModelScope)

  val isServerUrlSet: Boolean = preferences.serverUrl.isSet()

  val loginToken: LoginToken? = preferences.loginToken.get()

  private val showStatusBar = preferences.showBottomBar.asStateFlow(viewModelScope)

  private val budgetName: Flow<String?> = budgetComponents.flatMapLatest { component ->
    component
      ?.localPreferences
      ?.map { meta -> meta[DbMetadata.BudgetName] }
      ?: flowOf(null)
  }

  val bottomBarState: StateFlow<BottomBarState> = viewModelScope.launchMolecule(Immediate) {
    val showStatusBar by showStatusBar.collectAsState()
    val budgetName by budgetName.collectAsState(initial = null)
    if (showStatusBar) {
      val apis by apiStateHolder.collectAsState()
      BottomBarState.Visible(
        isConnected = apis != null,
        budgetName = budgetName,
      )
    } else {
      BottomBarState.Hidden
    }
  }

  fun start() {
    connectionMonitor.start()
    viewModelScope.launch {
      serverVersionFetcher.startFetching()
    }
  }

  fun onDestroy() {
    Logger.v("onDestroy")
    connectionMonitor.stop()
    scope.cancel()

    with(files) {
      val tmpDir = tmp(mkdirs = false)
      fileSystem.deleteRecursively(tmpDir)
    }
  }
}
