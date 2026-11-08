package aktual.prefs.vm.search

import aktual.core.l10n.StringLookup
import aktual.core.model.Coordinates
import aktual.core.utils.LocationProvider
import aktual.prefs.vm.search.SearchSettingsState.Results
import aktual.prefs.vm.security.ShouldShowSecuritySettings
import aktual.test.assertThatNextEmissionIsEqualTo
import aktual.test.viewModelContext
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.StringResource

class SearchSettingsViewModelTest {
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
      viewModel.setQuery("definitely not a setting")
      assertThatNextEmissionIsEqualTo(NoResults)
    }
  }

  @Test
  fun `Matches a group and the preferences in it, with their paths`() = runTest {
    val viewModel = buildViewModel()

    viewModel.setQuery("dialogs")

    viewModel.state.test {
      assertThat(awaitResults())
        .containsExactly(
          SettingsSearchResult(
            title = "ui dialogs title",
            kind = Group,
            page = SystemUi,
            path = persistentListOf("ui group"),
          ),
          SettingsSearchResult(
            title = "ui blur dialogs",
            kind = Preference,
            page = SystemUi,
            path = persistentListOf("ui group", "ui dialogs title"),
          ),
        )
    }
  }

  @Test
  fun `Pages come before groups and preferences`() = runTest {
    val viewModel = buildViewModel()

    viewModel.setQuery("format")

    viewModel.state.test {
      val results = awaitResults()
      assertThat(results.first())
        .isEqualTo(
          SettingsSearchResult(
            title = "format group",
            kind = Page,
            page = Format,
            path = persistentListOf(),
          ),
        )
      assertThat(results.map { it.kind }.distinct())
        .containsExactly(
          SettingsSearchKind.Page,
          SettingsSearchKind.Group,
          SettingsSearchKind.Preference,
        )
    }
  }

  @Test
  fun `Same preference in two groups gives two results`() = runTest {
    val viewModel = buildViewModel()

    viewModel.setQuery("blur radius")

    viewModel.state.test {
      assertThat(awaitResults().map { it.path.last() })
        .containsExactly("ui bars title", "ui dialogs title")
    }
  }

  @Test
  fun `Location settings hidden when unsupported`() = runTest {
    val viewModel = buildViewModel(locationSupported = false)

    viewModel.setQuery("security")

    viewModel.state.test {
      val results = if (ShouldShowSecuritySettings) awaitResults() else emptyList()
      assertThat(results.filter { "location" in it.title }).isEmpty()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Security settings only shown where supported`() = runTest {
    val viewModel = buildViewModel()

    viewModel.setQuery("app lock")

    viewModel.state.test {
      if (ShouldShowSecuritySettings) {
        assertThat(awaitResults().map { it.page }).containsExactly(SettingsPage.Security)
      } else {
        advanceUntilIdle()
        assertThat(expectMostRecentItem()).isEqualTo(NoResults)
      }
    }
  }

  private suspend fun ReceiveTurbine<SearchSettingsState>.awaitResults():
    List<
      SettingsSearchResult,
    > {
    var state = awaitItem()
    while (state !is Results) state = awaitItem()
    cancelAndIgnoreRemainingEvents()
    return state.settings
  }

  private fun TestScope.buildViewModel(locationSupported: Boolean = true) =
    SearchSettingsViewModel(
      savedState = SavedStateHandle(),
      strings = FakeStringLookup,
      locationProvider = FakeLocationProvider(locationSupported),
      viewModelContext = viewModelContext,
    )

  // "settings_ui_blur_radius" -> "ui blur radius"
  private object FakeStringLookup : StringLookup {
    override suspend fun get(resource: StringResource): String =
      resource.key.removePrefix("settings_").replace('_', ' ')
  }

  private class FakeLocationProvider(override val isSupported: Boolean) : LocationProvider {
    override fun hasPermission() = true

    override suspend fun currentLocation(): Coordinates? = null
  }
}
