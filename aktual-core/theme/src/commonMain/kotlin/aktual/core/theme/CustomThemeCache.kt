package aktual.core.theme

import kotlinx.coroutines.flow.Flow

interface CustomThemeCache {
  suspend fun repos(): List<CustomThemeRepo>

  suspend fun summaries(): List<CustomThemeSummary>

  suspend fun theme(repo: CustomThemeRepo): CustomColors?

  /** Themes edited in-app, re-emitting whenever one is saved or deleted. */
  fun localThemes(): Flow<List<CustomColors>>

  suspend fun save(summaries: List<CustomThemeSummary>)

  suspend fun save(theme: CustomColors)

  suspend fun delete(repo: CustomThemeRepo)

  /** Clears downloaded themes and summaries. Local themes are user data, so they're kept. */
  suspend fun clear()
}
