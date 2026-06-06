package aktual.core.theme

class FakeCustomThemeCache : CustomThemeCache {
  private val storedThemes = mutableMapOf<CustomThemeRepo, CustomColors>()
  private var storedSummaries: List<CustomThemeSummary> = emptyList()
  val savedThemes = mutableListOf<CustomColors>()

  fun putTheme(theme: CustomColors) {
    storedThemes[theme.repo] = theme
  }

  fun putSummaries(summaries: List<CustomThemeSummary>) {
    storedSummaries = summaries
  }

  override suspend fun repos(): List<CustomThemeRepo> = storedThemes.keys.toList()

  override suspend fun summaries(): List<CustomThemeSummary> = storedSummaries

  override suspend fun theme(repo: CustomThemeRepo): CustomColors? = storedThemes[repo]

  override suspend fun save(summaries: List<CustomThemeSummary>) {
    storedSummaries = summaries
  }

  override suspend fun save(theme: CustomColors) {
    savedThemes += theme
    storedThemes[theme.repo] = theme
  }

  override suspend fun clear() {
    storedThemes.clear()
    savedThemes.clear()
    storedSummaries = emptyList()
  }
}
