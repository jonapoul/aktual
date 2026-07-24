package aktual.api.client

import aktual.core.theme.CustomColors
import aktual.core.theme.CustomThemeSummary

interface ThemeApi {
  suspend fun fetchCatalog(): List<CustomThemeSummary>

  suspend fun fetchTheme(summary: CustomThemeSummary): CustomColors
}
