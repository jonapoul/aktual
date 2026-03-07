package aktual.core.theme

import kotlinx.coroutines.flow.Flow

interface ThemeResolver {
  fun activeTheme(isSystemInDarkTheme: Boolean): Flow<Theme>

  suspend fun resolve(id: Theme.Id): Theme?
}
