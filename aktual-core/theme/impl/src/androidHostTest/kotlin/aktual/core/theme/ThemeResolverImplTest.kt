package aktual.core.theme

import aktual.core.model.ThemeId
import aktual.core.prefs.ThemePreferences
import aktual.core.prefs.ThemePreferencesImpl
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
class ThemeResolverImplTest {
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
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)
    }
  }

  @Test
  fun `System default in light mode returns LightTheme`() = runThemeTest {
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `System default in dark mode with Midnight night theme returns MidnightTheme`() =
    runThemeTest {
      preferences.nightTheme.set(MidnightTheme.id)
      resolver.activeTheme(isSystemInDarkTheme = true).test {
        assertThat(awaitItem()).isSameInstanceAs(MidnightTheme)
      }
    }

  @Test
  fun `System default in light mode ignores night theme preference`() = runThemeTest {
    preferences.nightTheme.set(MidnightTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `System default with custom night theme resolves from cache`() = runThemeTest {
    cache.putTheme(ShadesOfCoffeeTheme)
    preferences.nightTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Constant theme null defaults to DarkTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)
    }
  }

  @Test
  fun `Constant theme set to Light returns LightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(LightTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `Constant theme set to Midnight returns MidnightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(MidnightTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(MidnightTheme)
    }
  }

  @Test
  fun `Custom theme resolved from cache`() = runThemeTest {
    cache.putTheme(ShadesOfCoffeeTheme)
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Custom theme fetched when not in cache but summaries are cached`() = runThemeTest {
    cache.putSummaries(listOf(ShadesOfCoffeeThemeSummary))
    api.themes[ShadesOfCoffeeThemeSummary] = ShadesOfCoffeeTheme
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Custom theme fetched via catalog when nothing is cached`() = runThemeTest {
    api.catalog = listOf(ShadesOfCoffeeThemeSummary)
    api.themes[ShadesOfCoffeeThemeSummary] = ShadesOfCoffeeTheme
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isEqualTo(ShadesOfCoffeeTheme)
    }
  }

  @Test
  fun `Fetched custom theme is saved to cache`() = runThemeTest {
    cache.putSummaries(listOf(ShadesOfCoffeeThemeSummary))
    api.themes[ShadesOfCoffeeThemeSummary] = ShadesOfCoffeeTheme
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
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
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)
    }
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `Unknown custom theme falls back based on system dark mode`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId("unknown/repo"))
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)
    }
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `Catalog fetch failure falls back based on system dark mode`() = runThemeTest {
    api.fetchCatalogException = RuntimeException("Network error")
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ShadesOfCoffeeTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)
    }
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `Invalid theme ID without slash falls back to LightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId("invalid-no-slash"))
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `Invalid theme ID with too many slashes falls back to LightTheme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    preferences.constantTheme.set(ThemeId("a/b/c"))
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)
    }
  }

  @Test
  fun `Changing useSystemDefault emits new theme`() = runThemeTest {
    preferences.constantTheme.set(MidnightTheme.id)
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)

      preferences.useSystemDefault.set(false)
      assertThat(awaitItem()).isSameInstanceAs(MidnightTheme)
    }
  }

  @Test
  fun `Changing night theme emits new theme`() = runThemeTest {
    resolver.activeTheme(isSystemInDarkTheme = true).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)

      preferences.nightTheme.set(MidnightTheme.id)
      assertThat(awaitItem()).isSameInstanceAs(MidnightTheme)
    }
  }

  @Test
  fun `Changing constant theme emits new theme`() = runThemeTest {
    preferences.useSystemDefault.set(false)
    resolver.activeTheme(isSystemInDarkTheme = false).test {
      assertThat(awaitItem()).isSameInstanceAs(DarkTheme)

      preferences.constantTheme.set(LightTheme.id)
      assertThat(awaitItem()).isSameInstanceAs(LightTheme)

      preferences.constantTheme.set(MidnightTheme.id)
      assertThat(awaitItem()).isSameInstanceAs(MidnightTheme)
    }
  }
}
