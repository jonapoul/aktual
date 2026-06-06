package aktual.core.theme

import aktual.core.model.ThemeId
import kotlinx.coroutines.flow.Flow

interface ThemeResolver {
  fun activeColors(isSystemInDarkTheme: Boolean): Flow<Colors>

  suspend fun resolve(id: ThemeId): Colors?
}
