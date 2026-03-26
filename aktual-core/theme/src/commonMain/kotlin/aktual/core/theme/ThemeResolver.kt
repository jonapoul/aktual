package aktual.core.theme

import aktual.core.model.ThemeId
import kotlinx.coroutines.flow.Flow

interface ThemeResolver {
  fun activeTheme(isSystemInDarkTheme: Boolean): Flow<Theme>

  suspend fun resolve(id: ThemeId): Theme?
}
