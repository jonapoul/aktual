package aktual.core.theme

import aktual.test.TemporaryFolder
import alakazam.test.TestCoroutineContexts
import alakazam.test.standardDispatcher
import app.cash.burst.InterceptTest
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isDataClassEqualTo
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class CustomColorsCacheImplTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  private lateinit var cache: CustomThemeCache

  private fun runCacheTest(block: suspend TestScope.() -> Unit) = runTest {
    cache =
      CustomThemeCacheImpl(
        appDirectory = { temporaryFolder.root },
        fileSystem = SYSTEM,
        contexts = TestCoroutineContexts(standardDispatcher),
      )
    block()
  }

  @Test
  fun `summaries returns empty list when no file exists`() = runCacheTest {
    assertThat(cache.summaries()).isEmpty()
  }

  @Test
  fun `repos returns empty list when no themes stored`() = runCacheTest {
    assertThat(cache.repos()).isEmpty()
  }

  @Test
  fun `theme returns null when repo is not cached`() = runCacheTest {
    assertThat(cache.theme(ShadesOfCoffeeTheme.repo)).isNull()
  }

  @Test
  fun `save and load summaries roundtrips correctly`() = runCacheTest {
    val summaries = listOf(ShadesOfCoffeeThemeSummary)
    cache.save(summaries)
    assertThat(cache.summaries()).isEqualTo(summaries)
  }

  @Test
  fun `save and load theme roundtrips correctly`() = runCacheTest {
    cache.save(ShadesOfCoffeeTheme)
    assertThat(cache.theme(ShadesOfCoffeeTheme.repo))
      .isNotNull()
      .isDataClassEqualTo(ShadesOfCoffeeTheme)
  }

  @Test
  fun `repos includes repo after saving theme`() = runCacheTest {
    cache.save(ShadesOfCoffeeTheme)
    assertThat(cache.repos()).containsExactlyInAnyOrder(ShadesOfCoffeeTheme.repo)
  }

  @Test
  fun `repos lists multiple repos after saving multiple themes`() = runCacheTest {
    val secondRepo = CustomThemeRepo(userName = "other", repoName = "dark-theme")
    val secondTheme = ShadesOfCoffeeTheme.copy(repo = secondRepo)
    cache.save(ShadesOfCoffeeTheme)
    cache.save(secondTheme)
    assertThat(cache.repos()).containsExactlyInAnyOrder(ShadesOfCoffeeTheme.repo, secondRepo)
  }

  @Test
  fun `overwriting summaries replaces existing`() = runCacheTest {
    cache.save(listOf(ShadesOfCoffeeThemeSummary))
    val updated = listOf(ShadesOfCoffeeThemeSummary.copy(name = "Updated"))
    cache.save(updated)
    assertThat(cache.summaries()).isEqualTo(updated)
  }

  @Test
  fun `theme returns null for unknown repo even when other themes are cached`() = runCacheTest {
    cache.save(ShadesOfCoffeeTheme)
    val unknown = CustomThemeRepo(userName = "nobody", repoName = "unknown")
    assertThat(cache.theme(unknown)).isNull()
  }

  @Test
  fun `local themes are listed and updated on save and delete`() = runCacheTest {
    val local = LightColors.toCustomColors("Mine", CustomThemeRepo.local("abc"))
    cache.localThemes().test {
      assertThat(awaitItem()).isEmpty()

      cache.save(local)
      assertThat(awaitItem()).containsExactly(local)

      cache.delete(local.repo)
      assertThat(awaitItem()).isEmpty()
    }
  }

  @Test
  fun `local themes are kept apart from downloaded ones`() = runCacheTest {
    val local = LightColors.toCustomColors("Mine", CustomThemeRepo.local("abc"))
    cache.save(ShadesOfCoffeeTheme)
    cache.save(local)
    assertThat(cache.repos()).containsExactly(ShadesOfCoffeeTheme.repo)

    cache.clear()
    assertThat(cache.theme(ShadesOfCoffeeTheme.repo)).isNull()
    assertThat(cache.theme(local.repo)).isEqualTo(local)
  }
}
