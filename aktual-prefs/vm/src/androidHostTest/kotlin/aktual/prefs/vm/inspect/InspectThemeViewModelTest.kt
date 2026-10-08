package aktual.prefs.vm.inspect

import aktual.core.model.ThemeId
import aktual.core.theme.Colors
import aktual.core.theme.LightColors
import aktual.core.theme.ThemeResolver
import aktual.prefs.vm.inspect.InspectThemeState.Loaded
import aktual.prefs.vm.theme.properties
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.standardDispatcher
import androidx.compose.ui.graphics.Color
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isTrue
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class InspectThemeViewModelTest {
  private val resolver =
    object : ThemeResolver {
      override fun activeColors(isSystemInDarkTheme: Boolean): Flow<Colors> = flowOf(LightColors)

      override suspend fun resolve(id: ThemeId): Colors = LightColors
    }

  private fun TestScope.buildViewModel(): InspectThemeViewModel {
    Dispatchers.setMain(standardDispatcher)
    return InspectThemeViewModel(LightColors.id, resolver, urlOpener = {})
  }

  @AfterTest
  fun after() {
    Dispatchers.resetMain()
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

  private fun Color.isGrey() = red == green && green == blue
}
