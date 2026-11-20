package aktual.prefs.vm.theme

import aktual.api.client.ThemeApi
import aktual.core.model.ThemeId
import aktual.core.theme.CustomColors
import aktual.core.theme.CustomThemeCache
import aktual.core.theme.CustomThemeSummary
import aktual.core.utils.runCatchingCancellable
import aktual.log
import alakazam.kotlin.requireMessage
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

// The themes listed by upstream's catalog, plus any downloaded ones it no longer lists
internal class ThemeCatalog(private val themeApi: ThemeApi, private val cache: CustomThemeCache) {
  private val summaries = MutableStateFlow(emptyList<CustomThemeSummary>())
  private val status = MutableStateFlow(CatalogStatus.Loading)

  // Only holds themes being downloaded or that failed, the rest come from the cache
  private val downloads = MutableStateFlow(persistentMapOf<ThemeId, DownloadState>())

  val state: Flow<CatalogState> =
    combine(summaries, status, downloads, cache.downloadedThemes()) {
      summaries,
      status,
      downloads,
      downloaded ->
      val downloadedById = downloaded.associateBy { it.id }
      val listed = summaries.map { summary ->
        val id = ThemeId(summary.repo.id)
        val state: DownloadState = downloads[id] ?: if (id in downloadedById) Downloaded else Remote
        CatalogItem(summary, state)
      }
      val listedIds = listed.mapTo(mutableSetOf()) { it.id }
      val unlisted =
        downloaded
          .filter { it.id !in listedIds }
          .map { CatalogItem(it.toSummary(), downloads[it.id] ?: Downloaded) }
      CatalogState((listed + unlisted).toImmutableList(), status)
    }

  // Reads the catalog saved on the device, only fetching it if there isn't one yet
  suspend fun load() {
    val cached = cache.summaries()
    if (cached.isEmpty()) {
      refresh()
    } else {
      summaries.update { cached }
      status.update { Idle }
    }
  }

  // Leaves the saved catalog and downloaded themes alone if the fetch fails
  suspend fun refresh(): Boolean {
    status.update { Loading }
    return runCatchingCancellable(
      attempt = {
        val fetched = themeApi.fetchCatalog()
        cache.save(fetched)
        summaries.update { fetched }
        updateDownloaded(fetched)
        status.update { Idle }
        true
      },
      onFailure = { e ->
        log.e(e) { "Failed fetching theme catalog" }
        status.update { Failed }
        false
      },
    )
  }

  suspend fun download(summary: CustomThemeSummary): DownloadResult {
    val id = ThemeId(summary.repo.id)
    cache.theme(summary.repo)?.let {
      return DownloadResult.Success(it, wasDownloaded = true)
    }

    downloads.update { it.putting(id, Downloading) }
    return runCatchingCancellable(
      attempt = {
        val theme = themeApi.fetchTheme(summary)
        cache.save(theme)
        downloads.update { it.removing(id) }
        DownloadResult.Success(theme, wasDownloaded = false)
      },
      onFailure = { e ->
        log.e(e) { "Failed fetching theme ${summary.repo}" }
        val reason = e.requireMessage()
        downloads.update { it.putting(id, DownloadState.Failed(reason)) }
        DownloadResult.Failure(reason)
      },
    )
  }

  // Picks up changes the authors made since. A theme that fails to update keeps its old colours
  private suspend fun updateDownloaded(summaries: List<CustomThemeSummary>) {
    val downloaded = cache.repos().toSet()
    summaries
      .filter { it.repo in downloaded }
      .forEach { summary ->
        runCatchingCancellable(
          attempt = { cache.save(themeApi.fetchTheme(summary)) },
          onFailure = { e -> log.w(e) { "Failed updating theme ${summary.repo}" } },
        )
      }
  }

  private fun CustomColors.toSummary() =
    CustomThemeSummary(
      name = name,
      repo = repo,
      colors = palette(),
      mode = if (isLight) Light else Dark,
    )
}

internal sealed interface DownloadResult {
  data class Success(val theme: CustomColors, val wasDownloaded: Boolean) : DownloadResult

  @JvmInline value class Failure(val reason: String) : DownloadResult
}
