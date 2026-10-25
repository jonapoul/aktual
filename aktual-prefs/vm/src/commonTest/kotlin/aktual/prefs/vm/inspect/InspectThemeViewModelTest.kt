package aktual.prefs.vm.inspect

import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.theme.CustomThemeRepo
import aktual.core.theme.LightColors
import aktual.core.theme.ThemeResolver
import aktual.core.theme.attributes
import aktual.core.theme.toCustomColors
import aktual.prefs.ThemePreferences
import aktual.prefs.ThemePreferencesImpl
import aktual.prefs.vm.inspect.InspectThemeEvent.Deleted
import aktual.prefs.vm.inspect.InspectThemeEvent.Duplicated
import aktual.prefs.vm.inspect.InspectThemeState.Loaded
import aktual.prefs.vm.theme.properties
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.buildPreferences
import aktual.test.viewModelContext
import alakazam.test.standardDispatcher
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

class InspectThemeViewModelTest {
  private val cache = FakeCustomThemeCache()
  private lateinit var preferences: ThemePreferences

  private val resolver =
    object : ThemeResolver {
      override fun activeColors(isSystemInDarkTheme: Boolean): Flow<Colors> = flowOf(LightColors)

      override suspend fun resolve(id: ThemeId): Colors =
        cache.themes.value.values.firstOrNull { it.id == id } ?: LightColors
    }

  private fun TestScope.buildViewModel(id: ThemeId = LightColors.id): InspectThemeViewModel {
    preferences = ThemePreferencesImpl(buildPreferences(standardDispatcher))
    return InspectThemeViewModel(
      themeId = id,
      themeResolver = resolver,
      cache = cache,
      preferences = preferences,
      uuidGenerator = { "abc" },
      viewModelContext = viewModelContext,
    )
  }

  @Test
  fun `Sorting reorders the properties`() = runTest {
    val viewModel = buildViewModel()
    val defaultNames = LightColors.properties().map { it.name }

    viewModel.state
      .filterIsInstance<Loaded>()
      .map { state -> state.properties.map { it.name } }
      .test {
        assertThatNextEmissionIsEqualTo(defaultNames)

        viewModel.setSorting(ByName)
        assertThatNextEmissionIsEqualTo(defaultNames.sortedBy { it.lowercase() })

        viewModel.setSorting(Default)
        assertThatNextEmissionIsEqualTo(defaultNames)

        cancelAndIgnoreRemainingEvents()
      }
  }

  @Test
  fun `Sorting by colour puts greys first then orders by hue`() = runTest {
    val viewModel = buildViewModel()
    viewModel.setSorting(ByColor)

    viewModel.state.filterIsInstance<Loaded>().test {
      val colors = awaitItem().properties.map { it.color }
      val firstColoured = colors.indexOfFirst { !it.isGrey() }
      assertThat(colors.take(firstColoured).all { it.isGrey() }).isTrue()
      assertThat(colors.drop(firstColoured).none { it.isGrey() }).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Duplicating saves a local copy and navigates to it`() = runTest {
    val viewModel = buildViewModel()
    viewModel.state.filterIsInstance<Loaded>().test {
      assertThat(awaitItem().source).isEqualTo(BuiltIn)
      cancelAndIgnoreRemainingEvents()
    }

    viewModel.events.test {
      viewModel.duplicate(name = "My theme")
      val repo = CustomThemeRepo.local("abc")
      assertThat(awaitItem()).isEqualTo(Duplicated(ThemeId(repo.id)))

      val copy = cache.themes.value.getValue(repo)
      assertThat(copy.name).isEqualTo("My theme")
      assertThat(copy.attributes()).isEqualTo(LightColors.attributes())
    }
  }

  @Test
  fun `Setting a colour edits and saves a local theme`() = runTest {
    val local = LightColors.toCustomColors("Mine", CustomThemeRepo.local("abc"))
    cache.save(local)
    val viewModel = buildViewModel(local.id)

    viewModel.state.filterIsInstance<Loaded>().test {
      assertThat(awaitItem().source).isEqualTo(Local)

      viewModel.setColor("tableRowBackgroundAlternate", Red)
      val edited = awaitItem().properties.first { it.name == "tableRowBackgroundAlternate" }
      assertThat(edited.color).isEqualTo(Red)

      advanceUntilIdle()
      val saved = cache.themes.value.getValue(local.repo)
      assertThat(saved.tableRowBackgroundAlternate).isEqualTo(Red)
      assertThat(saved.tableBackground).isEqualTo(LightColors.tableBackground)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Setting a colour on a built-in theme does nothing`() = runTest {
    val viewModel = buildViewModel()
    viewModel.state.filterIsInstance<Loaded>().test {
      awaitItem()
      viewModel.setColor("pageBackground", Red)
      advanceUntilIdle()
      expectNoEvents()
    }
    assertThat(cache.themes.value).isEmpty()
  }

  @Test
  fun `Deleting a selected local theme removes it and resets the selection`() = runTest {
    val local = LightColors.toCustomColors("Mine", CustomThemeRepo.local("abc"))
    cache.save(local)
    val other = LightColors.toCustomColors("Other", CustomThemeRepo.local("def"))
    cache.save(other)
    val viewModel = buildViewModel(local.id)
    preferences.constantTheme.set(local.id)

    viewModel.state.filterIsInstance<Loaded>().test {
      awaitItem()
      cancelAndIgnoreRemainingEvents()
    }

    viewModel.events.test {
      viewModel.delete()
      assertThat(awaitItem()).isEqualTo(Deleted)
    }
    assertThat(cache.themes.value.keys).containsOnly(other.repo)
    assertThat(preferences.constantTheme.get()).isNull()
  }

  private fun Color.isGrey() = red == green && green == blue
}
