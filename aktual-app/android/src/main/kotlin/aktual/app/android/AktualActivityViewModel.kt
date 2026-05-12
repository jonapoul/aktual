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
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

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
  )
