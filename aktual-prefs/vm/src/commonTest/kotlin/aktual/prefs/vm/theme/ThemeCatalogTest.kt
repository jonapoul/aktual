package aktual.prefs.vm.theme

import aktual.api.client.ThemeApi
import aktual.core.model.ThemeId
import aktual.core.theme.CustomColors
import aktual.core.theme.CustomThemeRepo
import aktual.core.theme.CustomThemeSummary
import aktual.core.theme.DarkColors
import aktual.core.theme.LightColors
import aktual.core.theme.toCustomColors
import aktual.prefs.vm.inspect.FakeCustomThemeCache
import aktual.test.assertThatNextEmissionIsEqualTo
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest

class ThemeCatalogTest {
  private val cache = FakeCustomThemeCache()
  private val api = FakeThemeApi()
  private val catalog = ThemeCatalog(api, cache)

  @Test
  fun `Failed refresh keeps downloaded themes`() = runTest {
    cache.save(listOf(Remote1, Downloaded1))
    cache.save(Downloaded1Colors)
    catalog.load()
    api.failure = IllegalStateException("Offline")

    assertThat(catalog.refresh()).isFalse()

    catalog.state.test {
      assertThatNextEmissionIsEqualTo(
        CatalogState(
          persistentListOf(
            CatalogItem(Remote1, Remote),
            CatalogItem(Downloaded1, Downloaded),
          ),
          Failed,
        ),
      )
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Downloaded themes show without a saved catalog`() = runTest {
    cache.save(Downloaded1Colors)
    api.failure = IllegalStateException("Offline")
    catalog.load()

    catalog.state.test {
      val state = awaitItem()
      assertThat(state.status).isEqualTo(Failed)
      assertThat(state.items.map { it.id to it.download })
        .isEqualTo(listOf(ThemeId(Downloaded1.repo.id) to DownloadState.Downloaded))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Refresh updates downloaded themes`() = runTest {
    cache.save(Downloaded1Colors)
    val updated = DarkColors.toCustomColors(Downloaded1.name, Downloaded1.repo)
    api.catalog = listOf(Remote1, Downloaded1)
    api.themes = mapOf(Downloaded1.repo to updated)

    catalog.refresh()

    assertThat(cache.theme(Downloaded1.repo)).isEqualTo(updated)
    assertThat(cache.theme(Remote1.repo)).isNull()
  }

  @Test
  fun `Failed download is shown on the item`() = runTest {
    api.catalog = listOf(Remote1)
    catalog.refresh()
    api.failure = IllegalStateException("Nope")

    val result = catalog.download(Remote1)

    assertThat(result).isInstanceOf<DownloadResult.Failure>()
    catalog.state.test {
      assertThat(awaitItem().items.single().download).isEqualTo(DownloadState.Failed("Nope"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  private class FakeThemeApi : ThemeApi {
    var catalog = emptyList<CustomThemeSummary>()
    var themes = emptyMap<CustomThemeRepo, CustomColors>()
    var failure: Exception? = null

    override suspend fun fetchCatalog(): List<CustomThemeSummary> {
      failure?.let { throw it }
      return catalog
    }

    override suspend fun fetchTheme(summary: CustomThemeSummary): CustomColors {
      failure?.let { throw it }
      return themes[summary.repo] ?: error("No theme for ${summary.repo}")
    }
  }

  private companion object {
    val Remote1 = summary("Remote", CustomThemeRepo("user", "remote"))
    val Downloaded1 = summary("Saved", CustomThemeRepo("user", "saved"))
    val Downloaded1Colors = LightColors.toCustomColors(Downloaded1.name, Downloaded1.repo)

    fun summary(name: String, repo: CustomThemeRepo) =
      CustomThemeSummary(name, repo, LightColors.palette(), mode = Light)
  }
}
