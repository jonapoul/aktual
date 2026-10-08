package aktual.prefs.vm.inspect.search

import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.theme.LightColors
import aktual.core.theme.ThemeResolver
import aktual.prefs.vm.inspect.ThemeProperty
import aktual.prefs.vm.inspect.search.SearchThemeState.Results
import aktual.prefs.vm.inspect.toHexString
import aktual.prefs.vm.theme.properties
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchThemeViewModelTest {
  private val resolver =
    object : ThemeResolver {
      override fun activeColors(isSystemInDarkTheme: Boolean): Flow<Colors> = flowOf(LightColors)

      override suspend fun resolve(id: ThemeId): Colors = LightColors
    }

  private val properties = LightColors.properties()

  private fun TestScope.buildViewModel(): SearchThemeViewModel {
    Dispatchers.setMain(standardDispatcher)
    return SearchThemeViewModel(LightColors.id, resolver)
  }

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
  }

  @Test
  fun `Blank query prompts for input`() = runTest {
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery("   ")
      advanceUntilIdle()
      expectNoEvents()
    }
  }

  @Test
  fun `Query matching nothing gives no results`() = runTest {
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery("definitely not a property")
      assertThatNextEmissionIsEqualTo(NoResults)
    }
  }

  @Test
  fun `Matches names case-insensitively`() = runTest {
    val viewModel = buildViewModel()
    viewModel.setQuery(" PAGETEXT ")

    viewModel.state.filterIsInstance<Results>().test {
      val results = awaitItem()
      assertThat(results.query).isEqualTo("PAGETEXT")
      assertThat(results.properties.map { it.name })
        .isEqualTo(properties.map { it.name }.filter { it.contains("pagetext", ignoreCase = true) })
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Matches hex colours`() = runTest {
    val viewModel = buildViewModel()
    val hex = properties.first().color.toHexString()
    val expected = properties.filter { hex in it.color.toHexString() }.map(ThemeProperty::name)
    viewModel.setQuery(hex.lowercase())

    viewModel.state.filterIsInstance<Results>().test {
      val names = awaitItem().properties.map { it.name }
      assertThat(names).isNotEmpty()
      assertThat(names).isEqualTo(expected)
      cancelAndIgnoreRemainingEvents()
    }
  }
}
