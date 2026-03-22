package aktual.app.desktop

import aktual.app.nav.AppLifecycleManager
import aktual.app.nav.BlurConfigUseCase
import aktual.app.nav.BottomBarStateUseCase
import aktual.app.nav.FormatConfigUseCase
import aktual.app.nav.InitialRouteUseCase
import aktual.app.nav.RootViewModel
import aktual.core.theme.ThemeResolver
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey(AktualDesktopViewModel::class)
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class AktualDesktopViewModel(
  themeResolver: ThemeResolver,
  appLifecycleManager: AppLifecycleManager,
  formatConfigUseCase: FormatConfigUseCase,
  blurConfigUseCase: BlurConfigUseCase,
  initialRouteUseCase: InitialRouteUseCase,
  bottomBarStateUseCase: BottomBarStateUseCase,
) :
  RootViewModel(
    themeResolver = themeResolver,
    appLifecycleManager = appLifecycleManager,
    formatConfigUseCase = formatConfigUseCase,
    blurConfigUseCase = blurConfigUseCase,
    initialRouteUseCase = initialRouteUseCase,
    bottomBarStateUseCase = bottomBarStateUseCase,
  )
