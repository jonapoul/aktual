package aktual.app.nav

import aktual.core.theme.Theme
import aktual.core.theme.ThemeResolver
import aktual.core.ui.BlurConfig
import aktual.core.ui.BottomBarState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Stable
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

  private var backStack: SnapshotStateList<NavKey>? = null

  private val isSystemInDarkTheme = MutableStateFlow<Boolean?>(null)

  val theme: StateFlow<Theme?> =
    isSystemInDarkTheme
      .filterNotNull()
      .flatMapLatest { isDark -> themeResolver.activeTheme(isDark) }
      .stateIn(viewModelScope, Eagerly, null)

  init {
    appLifecycleManager.start(viewModelScope)
  }

  fun getOrCreateBackStack(initialRoute: NavKey): SnapshotStateList<NavKey> =
    backStack ?: mutableStateListOf(initialRoute).also { backStack = it }

  fun updateSystemDarkTheme(isSystemInDarkTheme: Boolean) {
    this.isSystemInDarkTheme.update { isSystemInDarkTheme }
  }

  fun onDestroy() = appLifecycleManager.destroy()
}
