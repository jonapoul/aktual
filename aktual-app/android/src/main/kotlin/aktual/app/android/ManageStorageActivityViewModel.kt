package aktual.app.android

import aktual.core.theme.Theme
import aktual.core.theme.ThemeResolver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ManageStorageActivityViewModel(private val themeResolver: ThemeResolver) : ViewModel() {
  private val isSystemInDarkTheme = MutableStateFlow<Boolean?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val theme: StateFlow<Theme?> =
    isSystemInDarkTheme
      .filterNotNull()
      .flatMapLatest { isDark -> themeResolver.activeTheme(isDark) }
      .stateIn(viewModelScope, Eagerly, null)

  fun updateSystemDarkTheme(isSystemInDarkTheme: Boolean) {
    this.isSystemInDarkTheme.update { isSystemInDarkTheme }
  }
}
