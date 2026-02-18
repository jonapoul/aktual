package aktual.core.theme

import aktual.core.ui.Theme

interface ThemeApi {
  suspend fun fetchThemeList(): List<ThemeSummary>

  suspend fun fetchTheme(repo: ThemeRepo): Theme
}
