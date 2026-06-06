package aktual.core.theme

interface CustomThemeCache {
  suspend fun repos(): List<CustomThemeRepo>

  suspend fun summaries(): List<CustomThemeSummary>

  suspend fun theme(repo: CustomThemeRepo): CustomColors?

  suspend fun save(summaries: List<CustomThemeSummary>)

  suspend fun save(theme: CustomColors)

  suspend fun clear()
}
