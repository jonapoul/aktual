package aktual.core.theme

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCustomThemeCache : CustomThemeCache {
  private val storedThemes = MutableStateFlow(emptyMap<CustomThemeRepo, CustomColors>())
  private var storedSummaries: List<CustomThemeSummary> = emptyList()
  val savedThemes = mutableListOf<CustomColors>()

  fun putTheme(theme: CustomColors) {
    storedThemes.update { it + (theme.repo to theme) }
  }

  fun putSummaries(summaries: List<CustomThemeSummary>) {
    storedSummaries = summaries
  }

  override suspend fun repos(): List<CustomThemeRepo> =
    storedThemes.value.keys.filterNot { it.isLocal }

  override suspend fun summaries(): List<CustomThemeSummary> = storedSummaries

  override suspend fun theme(repo: CustomThemeRepo): CustomColors? = storedThemes.value[repo]

  override fun localThemes(): Flow<List<CustomColors>> = storedThemes.map { themes ->
    themes.values.filter { it.repo.isLocal }
  }

  override suspend fun save(summaries: List<CustomThemeSummary>) {
    storedSummaries = summaries
  }

  override suspend fun save(theme: CustomColors) {
    savedThemes += theme
    storedThemes.update { it + (theme.repo to theme) }
  }

  override suspend fun delete(repo: CustomThemeRepo) {
    storedThemes.update { it - repo }
  }

  override suspend fun clear() {
    storedThemes.update { themes -> themes.filterKeys { it.isLocal } }
    savedThemes.clear()
    storedSummaries = emptyList()
  }
}
