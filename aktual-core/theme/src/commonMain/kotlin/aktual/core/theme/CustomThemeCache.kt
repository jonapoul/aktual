package aktual.core.theme

interface CustomThemeCache {
  suspend fun repos(): List<CustomThemeRepo>

  suspend fun summaries(): List<CustomThemeSummary>

  suspend fun theme(repo: CustomThemeRepo): CustomTheme?

  suspend fun save(summaries: List<CustomThemeSummary>)

  suspend fun save(theme: CustomTheme)

  suspend fun clear()
}
