package aktual.core.theme

import aktual.core.model.ThemeId
import aktual.prefs.ThemePreferences
import aktual.prefs.ThemePreferencesImpl
import aktual.test.buildPreferences
import androidx.compose.ui.graphics.Color.Companion.Red
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

class LocalThemeResolverTest {
  private lateinit var api: FakeThemeApi
  private lateinit var cache: FakeCustomThemeCache
  private lateinit var preferences: ThemePreferences
  private lateinit var resolver: ThemeResolverImpl

  private fun runThemeTest(block: suspend TestScope.() -> Unit) = runTest {
    api = FakeThemeApi()
    cache = FakeCustomThemeCache()
    preferences = ThemePreferencesImpl(buildPreferences(coroutineContext))
    resolver = ThemeResolverImpl(preferences, api, cache)
    block()
  }

  @Test
  fun `Missing local theme falls back without fetching`() = runThemeTest {
    api.fetchCatalogException = RuntimeException("Shouldn't fetch")
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId(CustomThemeRepo.local("gone").id))
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)
    }
  }

  @Test
  fun `Editing the active local theme emits the edited theme`() = runThemeTest {
    val local = LightColors.toCustomColors("Mine", CustomThemeRepo.local("abc"))
    cache.save(local)
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(local.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(local)

      val edited = local.toCustomColors(local.name, local.repo, mapOf("pageText" to Red))
      cache.save(edited)
      assertThat(awaitItem()).isEqualTo(edited)
    }
  }
}
