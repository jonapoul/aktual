package aktual.core.theme

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import logcat.logcat

@ContributesBinding(AppScope::class)
class ThemeResolverImpl(
  private val preferences: ThemePreferences,
  private val api: ThemeApi,
  private val cache: CustomThemeCache,
) : ThemeResolver {
  override fun activeTheme(isSystemInDarkTheme: Boolean): Flow<Theme> =
    combine(
        preferences.useSystemDefault.asFlow(),
        preferences.nightTheme.asFlow(),
        preferences.constantTheme.asFlow(),
      ) { useSystemDefault, nightThemeId, customThemeId ->
        if (useSystemDefault) {
          if (isSystemInDarkTheme) nightThemeId else LightTheme.id
        } else {
          customThemeId ?: DarkTheme.id
        }
      }
      .map { id -> resolveWithFallback(id, isSystemInDarkTheme) }

  override suspend fun resolve(id: Theme.Id): Theme? {
    Theme.Defaults.firstOrNull { it.id == id }
      ?.let {
        return it
      }
    val repo = id.toRepo() ?: return null
    return cache.theme(repo)
  }

  private suspend fun resolveWithFallback(id: Theme.Id, isSystemInDarkTheme: Boolean): Theme {
    resolve(id)?.let {
      return it
    }
    val repo = id.toRepo()
    if (repo == null) {
      logcat.e { "Failed to convert '$id' to a custom theme repository" }
      return LightTheme
    }
    return fetchAndCache(repo, isSystemInDarkTheme)
  }

  private suspend fun fetchAndCache(repo: CustomThemeRepo, isSystemInDarkTheme: Boolean): Theme {
    try {
      val summaries = cache.summaries().ifEmpty { api.fetchCatalog() }
      val summary =
        summaries.firstOrNull { it.repo == repo } ?: return fallback(isSystemInDarkTheme)
      val theme = api.fetchTheme(summary)
      cache.save(theme)
      return theme
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      val default = fallback(isSystemInDarkTheme)
      logcat.w(e) { "Failed fetching theme from $repo, returning $default" }
      return default
    }
  }

  private fun Theme.Id.toRepo(): CustomThemeRepo? {
    if ('/' !in value) return null
    val parts = value.split('/')
    if (parts.size != 2) return null
    return CustomThemeRepo(userName = parts[0], repoName = parts[1])
  }

  private fun fallback(isSystemInDarkTheme: Boolean): DefaultTheme =
    if (isSystemInDarkTheme) DarkTheme else LightTheme
}
