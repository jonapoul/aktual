package aktual.app.android

import aktual.app.nav.AppLifecycleManager
import aktual.app.nav.BottomBarStateUseCase
import aktual.app.nav.FormatConfigUseCase
import aktual.app.nav.HazeConfigUseCase
import aktual.app.nav.InitialRouteUseCase
import aktual.app.nav.RootViewModel
import aktual.core.nav.NavEntryContributor
import aktual.core.utils.LocalNetworkPermission
import aktual.core.utils.LocalNetworkPrompt
import aktual.di.AppScope
import aktual.di.RunLevelState
import aktual.di.ViewModelContext
import aktual.prefs.AppPreferences
import aktual.prefs.SecurityPreferences
import aktual.theme.ThemeResolver
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
@Suppress("LongParameterList")
class AktualActivityViewModel(
  themeResolver: ThemeResolver,
  appLifecycleManager: AppLifecycleManager,
  navEntryContributors: Set<NavEntryContributor>,
  formatConfigUseCase: FormatConfigUseCase,
  hazeConfigUseCase: HazeConfigUseCase,
  initialRouteUseCase: InitialRouteUseCase,
  bottomBarStateUseCase: BottomBarStateUseCase,
  securityPreferences: SecurityPreferences,
  private val appPreferences: AppPreferences,
  private val localNetworkPermission: LocalNetworkPermission,
  private val localNetworkPrompt: LocalNetworkPrompt,
  val runLevels: RunLevelState,
  viewModelContext: ViewModelContext,
) :
  RootViewModel(
    themeResolver = themeResolver,
    appLifecycleManager = appLifecycleManager,
    navEntryContributors = navEntryContributors,
    formatConfigUseCase = formatConfigUseCase,
    hazeConfigUseCase = hazeConfigUseCase,
    initialRouteUseCase = initialRouteUseCase,
    bottomBarStateUseCase = bottomBarStateUseCase,
    runLevels = runLevels,
    viewModelContext = viewModelContext,
  ) {
  // hides the app's contents in the recent-apps switcher (and blocks screenshots) when true. Forced
  // on by the app lock, since the recents snapshot is taken before the lock screen can draw
  val secureWindow: StateFlow<Boolean> =
    combine(
        securityPreferences.hidePreviewInAppSwitcher.asFlow(),
        securityPreferences.appLockEnabled.asFlow(),
      ) { hidePreview, lockEnabled ->
        hidePreview || lockEnabled
      }
      .stateIn(viewModelScope, Eagerly, initialValue = true)

  private val hasCheckedLocalNetworkPermission = MutableStateFlow(value = false)

  // For a server saved before updating to Android 17, or before the permission was revoked. True at
  // most once per VM, so recreating the activity or unlocking the app doesn't ask again. Holds
  // server requests from the start of the check until onLocalNetworkPermissionResult
  suspend fun needsLocalNetworkPermission(): Boolean {
    if (hasCheckedLocalNetworkPermission.value) return false
    val isNeeded = checkLocalNetworkPermission()
    // Only once it's finished, so a check cancelled mid-lookup runs again in the new composition
    hasCheckedLocalNetworkPermission.update { true }
    return isNeeded
  }

  private suspend fun checkLocalNetworkPermission(): Boolean {
    val url = appPreferences.serverUrl.get() ?: return false
    if (localNetworkPermission.isGranted()) return false

    localNetworkPrompt.onShowing()
    var isRequired = false
    try {
      isRequired = localNetworkPermission.isRequiredFor(url)
    } finally {
      // Also on cancellation, e.g. the app locking mid-lookup, so requests don't wait for nothing
      if (!isRequired) localNetworkPrompt.onAnswered()
    }
    return isRequired
  }

  fun onLocalNetworkPermissionResult() = localNetworkPrompt.onAnswered()
}
