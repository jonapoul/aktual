package aktual.core.theme

import aktual.core.model.ThemeId
import aktual.prefs.ThemePreferences
import aktual.prefs.ThemePreferencesImpl
import aktual.test.buildPreferences
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isSameInstanceAs
import kotlin.test.Test
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ColorsResolverImplTest {
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
  fun `System default in dark mode returns DarkTheme`() = runThemeTest {
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)
    }
  }

  @Test
  fun `System default in light mode returns LightTheme`() = runThemeTest {
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `System default in dark mode with Midnight night theme returns MidnightTheme`() =
    runThemeTest {
      preferences.nightTheme.set(MidnightColors.id)
      resolver.activeColors(isSystemInDarkTheme = true).test {
        assertThat(awaitItem()).isSameInstanceAs(MidnightColors)
      }
    }

  @Test
  fun `System default in light mode ignores night theme preference`() = runThemeTest {
    preferences.nightTheme.set(MidnightColors.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `System default with custom night theme resolves from cache`() = runThemeTest {
    cache.putTheme(ShadesOfCoffeeTheme)
    preferences.nightTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Constant theme null defaults to DarkTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)
    }
  }

  @Test
  fun `Constant theme set to Light returns LightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(LightColors.id)
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `Constant theme set to Midnight returns MidnightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(MidnightColors.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(MidnightColors)
    }
  }

  @Test
  fun `Custom theme resolved from cache`() = runThemeTest {
    cache.putTheme(ShadesOfCoffeeTheme)
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Custom theme fetched when not in cache but summaries are cached`() = runThemeTest {
    cache.putSummaries(listOf(ShadesOfCoffeeThemeSummary))
    api.themes[ShadesOfCoffeeThemeSummary] = ShadesOfCoffeeTheme
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Custom theme fetched via catalog when nothing is cached`() = runThemeTest {
    api.catalog = listOf(ShadesOfCoffeeThemeSummary)
    api.themes[ShadesOfCoffeeThemeSummary] = ShadesOfCoffeeTheme
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Fetched custom theme is saved to cache`() = runThemeTest {
    cache.putSummaries(listOf(ShadesOfCoffeeThemeSummary))
    api.themes[ShadesOfCoffeeThemeSummary] = ShadesOfCoffeeTheme
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      awaitItem()
      assertThat(cache.savedThemes).containsExactly(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Custom theme fetch failure falls back based on system dark mode`() = runThemeTest {
    cache.putSummaries(listOf(ShadesOfCoffeeThemeSummary))
    api.fetchThemeException = RuntimeException("Network error")
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)
    }
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `Unknown custom theme falls back based on system dark mode`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId("unknown/repo"))
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)
    }
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `Catalog fetch failure falls back based on system dark mode`() = runThemeTest {
    api.fetchCatalogException = RuntimeException("Network error")
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)
    }
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `Invalid theme ID without slash falls back to LightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId("invalid-no-slash"))
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `Invalid theme ID with too many slashes falls back to LightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId("a/b/c"))
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(LightColors)
    }
  }

  @Test
  fun `Changing useSystemDefault emits new theme`() = runThemeTest {
    preferences.constantTheme.set(MidnightColors.id)
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)

      preferences.useSystemDefault.set(false)
      assertThat(awaitItem()).isSameInstanceAs(MidnightColors)
    }
  }

  @Test
  fun `Changing night theme emits new theme`() = runThemeTest {
    resolver.activeColors(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)

      preferences.nightTheme.set(MidnightColors.id)
      assertThat(awaitItem()).isSameInstanceAs(MidnightColors)
    }
  }

  @Test
  fun `Changing constant theme emits new theme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    resolver.activeColors(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkColors)

      preferences.constantTheme.set(LightColors.id)
      assertThat(awaitItem()).isSameInstanceAs(LightColors)

      preferences.constantTheme.set(MidnightColors.id)
      assertThat(awaitItem()).isSameInstanceAs(MidnightColors)
    }
  }
}
