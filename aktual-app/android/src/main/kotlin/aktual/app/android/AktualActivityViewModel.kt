package aktual.app.android

import aktual.app.nav.AppLifecycleManager
import aktual.app.nav.BlurConfigUseCase
import aktual.app.nav.BottomBarStateUseCase
import aktual.app.nav.FormatConfigUseCase
import aktual.app.nav.InitialRouteUseCase
import aktual.app.nav.NavGraph
import aktual.app.nav.RootViewModel
import aktual.core.theme.ThemeResolver
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@ViewModelKey(AktualActivityViewModel::class)
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class AktualActivityViewModel(
  themeResolver: ThemeResolver,
  appLifecycleManager: AppLifecycleManager,
  navGraphFactory: NavGraph.Factory,
  formatConfigUseCase: FormatConfigUseCase,
  blurConfigUseCase: BlurConfigUseCase,
  initialRouteUseCase: InitialRouteUseCase,
  bottomBarStateUseCase: BottomBarStateUseCase,
) :
  RootViewModel(
    themeResolver = themeResolver,
    appLifecycleManager = appLifecycleManager,
    navGraphFactory = navGraphFactory,
    formatConfigUseCase = formatConfigUseCase,
    blurConfigUseCase = blurConfigUseCase,
    initialRouteUseCase = initialRouteUseCase,
    bottomBarStateUseCase = bottomBarStateUseCase,
  )
