package aktual.app.android

import aktual.core.theme.Theme
import aktual.core.theme.ThemeResolver
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.Flow

@ViewModelKey(ManageStorageActivityViewModel::class)
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class ManageStorageActivityViewModel(private val themeResolver: ThemeResolver) : ViewModel() {
  fun theme(isSystemInDarkTheme: Boolean): Flow<Theme> =
    themeResolver.activeTheme(isSystemInDarkTheme)
}
