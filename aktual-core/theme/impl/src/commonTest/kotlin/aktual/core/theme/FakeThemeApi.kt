package aktual.core.theme

class FakeThemeApi : ThemeApi {
  var catalog: List<CustomThemeSummary> = emptyList()
  val themes = mutableMapOf<CustomThemeSummary, CustomTheme>()
  var fetchCatalogException: Exception? = null
  var fetchThemeException: Exception? = null

  override suspend fun fetchCatalog(): List<CustomThemeSummary> {
    fetchCatalogException?.let { throw it }
    return catalog
  }

  override suspend fun fetchTheme(summary: CustomThemeSummary): CustomTheme {
    fetchThemeException?.let { throw it }
    return themes[summary] ?: error("No theme configured for $summary")
  }
}
