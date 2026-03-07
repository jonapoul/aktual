package aktual.core.theme

interface ThemeApi {
  suspend fun fetchCatalog(): List<CustomThemeSummary>

  suspend fun fetchTheme(summary: CustomThemeSummary): CustomTheme
}
