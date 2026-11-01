package aktual.app.android

import aktual.app.nav.AppLifecycleManager
import aktual.app.nav.BottomBarStateUseCase
import aktual.app.nav.FormatConfigUseCase
import aktual.app.nav.HazeConfigUseCase
import aktual.app.nav.InitialRouteUseCase
import aktual.app.nav.RootViewModel
import aktual.core.nav.NavEntryContributor
import aktual.core.theme.ThemeResolver
import aktual.di.AppScope
import aktual.di.RunLevelState
import aktual.di.ViewModelContext
import aktual.prefs.SecurityPreferences
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

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
}
