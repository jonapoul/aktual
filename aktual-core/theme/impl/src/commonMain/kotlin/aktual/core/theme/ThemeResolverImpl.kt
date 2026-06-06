package aktual.core.theme

import aktual.core.model.ThemeId
import aktual.di.AppScope
import aktual.prefs.ThemePreferences
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
  override fun activeColors(isSystemInDarkTheme: Boolean): Flow<Colors> =
    combine(
        preferences.useSystemDefault.asFlow(),
        preferences.nightTheme.asFlow(),
        preferences.constantTheme.asFlow(),
      ) { useSystemDefault, nightThemeId, customThemeId ->
        if (useSystemDefault) {
          if (isSystemInDarkTheme) nightThemeId else LightColors.id
        } else {
          customThemeId ?: DarkColors.id
        }
      }
      .map { id -> resolveWithFallback(id, isSystemInDarkTheme) }

  override suspend fun resolve(id: ThemeId): Colors? {
    Colors.Defaults.firstOrNull { it.id == id }
      ?.let {
        return it
      }
    val repo = id.toRepo() ?: return null
    return cache.theme(repo)
  }

  private suspend fun resolveWithFallback(id: ThemeId, isSystemInDarkTheme: Boolean): Colors {
    resolve(id)?.let {
      return it
    }
    val repo = id.toRepo()
    if (repo == null) {
      logcat.e { "Failed to convert '$id' to a custom theme repository" }
      return LightColors
    }
    return fetchAndCache(repo, isSystemInDarkTheme)
  }

  private suspend fun fetchAndCache(repo: CustomThemeRepo, isSystemInDarkTheme: Boolean): Colors {
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

  private fun ThemeId.toRepo(): CustomThemeRepo? {
    if ('/' !in value) return null
    val parts = value.split('/')
    if (parts.size != 2) return null
    return CustomThemeRepo(userName = parts[0], repoName = parts[1])
  }

  private fun fallback(isSystemInDarkTheme: Boolean): DefaultColors =
    if (isSystemInDarkTheme) DarkColors else LightColors
}
