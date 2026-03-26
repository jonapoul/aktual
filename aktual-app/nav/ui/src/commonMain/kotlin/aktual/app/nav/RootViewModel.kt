package aktual.app.nav

import aktual.core.theme.Theme
import aktual.core.theme.ThemeResolver
import aktual.core.ui.BlurConfig
import aktual.core.ui.BottomBarState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Suppress("AbstractClassCanBeConcreteClass")
abstract class RootViewModel(
  private val themeResolver: ThemeResolver,
  private val appLifecycleManager: AppLifecycleManager,
  navGraphFactory: NavGraph.Factory,
  formatConfigUseCase: FormatConfigUseCase,
  blurConfigUseCase: BlurConfigUseCase,
  initialRouteUseCase: InitialRouteUseCase,
  bottomBarStateUseCase: BottomBarStateUseCase,
) : ViewModel() {
  val navEntryContributors: ImmutableSet<NavEntryContributor> =
    navGraphFactory.create().navEntryContributors.toImmutableSet()
  val formatConfig: StateFlow<FormatConfig> = formatConfigUseCase(viewModelScope)
  val blurConfig: StateFlow<BlurConfig> = blurConfigUseCase(viewModelScope)
  val initialRoute: StateFlow<NavKey?> = initialRouteUseCase(viewModelScope)
  val bottomBarState: StateFlow<BottomBarState> = bottomBarStateUseCase(viewModelScope)
  val tokenExpired: Flow<Unit> = appLifecycleManager.tokenExpired

  init {
    appLifecycleManager.start(viewModelScope)
  }

  fun theme(isSystemInDarkTheme: Boolean): Flow<Theme> =
    themeResolver.activeTheme(isSystemInDarkTheme)

  fun onDestroy() = appLifecycleManager.destroy()
}
