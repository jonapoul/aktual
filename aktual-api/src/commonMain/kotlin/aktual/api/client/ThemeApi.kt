package aktual.api.client

import aktual.theme.CustomColors
import aktual.theme.CustomThemeSummary

interface ThemeApi {
  suspend fun fetchCatalog(): List<CustomThemeSummary>

  suspend fun fetchTheme(summary: CustomThemeSummary): CustomColors
}
