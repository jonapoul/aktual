package aktual.prefs.vm.inspect

import aktual.core.theme.CustomColors
import aktual.core.theme.CustomThemeCache
import aktual.core.theme.CustomThemeRepo
import aktual.core.theme.CustomThemeSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCustomThemeCache : CustomThemeCache {
  val themes = MutableStateFlow(emptyMap<CustomThemeRepo, CustomColors>())

  override suspend fun repos(): List<CustomThemeRepo> = themes.value.keys.toList()

  override suspend fun summaries(): List<CustomThemeSummary> = emptyList()

  override suspend fun theme(repo: CustomThemeRepo): CustomColors? = themes.value[repo]

  override fun localThemes(): Flow<List<CustomColors>> = themes.map { map ->
    map.values.filter { it.repo.isLocal }
  }

  override suspend fun save(summaries: List<CustomThemeSummary>) = Unit

  override suspend fun save(theme: CustomColors) {
    themes.update { it + (theme.repo to theme) }
  }

  override suspend fun delete(repo: CustomThemeRepo) {
    themes.update { it - repo }
  }

  override suspend fun clear() {
    themes.update { map -> map.filterKeys { it.isLocal } }
  }
}
