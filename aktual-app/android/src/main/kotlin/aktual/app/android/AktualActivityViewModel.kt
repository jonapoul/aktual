package aktual.app.android

import aktual.app.nav.AppLifecycleManager
import aktual.app.nav.BlurConfigUseCase
import aktual.app.nav.BottomBarStateUseCase
import aktual.app.nav.FormatConfigUseCase
import aktual.app.nav.InitialRouteUseCase
import aktual.app.nav.RootViewModel
import aktual.core.nav.NavEntryContributor
import aktual.core.theme.ThemeResolver
import aktual.di.AppScope
import aktual.di.RunLevelState
import aktual.prefs.SystemUiPreferences
import aktual.prefs.asStateFlow
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.StateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class AktualActivityViewModel(
  themeResolver: ThemeResolver,
  appLifecycleManager: AppLifecycleManager,
  navEntryContributors: Set<NavEntryContributor>,
  formatConfigUseCase: FormatConfigUseCase,
  blurConfigUseCase: BlurConfigUseCase,
  initialRouteUseCase: InitialRouteUseCase,
  bottomBarStateUseCase: BottomBarStateUseCase,
  systemUiPreferences: SystemUiPreferences,
  val runLevels: RunLevelState,
) :
  RootViewModel(
    themeResolver = themeResolver,
    appLifecycleManager = appLifecycleManager,
    navEntryContributors = navEntryContributors,
    formatConfigUseCase = formatConfigUseCase,
    blurConfigUseCase = blurConfigUseCase,
    initialRouteUseCase = initialRouteUseCase,
    bottomBarStateUseCase = bottomBarStateUseCase,
    runLevels = runLevels,
  ) {
  // hides the app's contents in the recent-apps switcher (and blocks screenshots) when true
  val hidePreviewInAppSwitcher: StateFlow<Boolean> =
    systemUiPreferences.hidePreviewInAppSwitcher.asStateFlow(viewModelScope)
}
