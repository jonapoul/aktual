package aktual.about.vm

import aktual.about.data.Apache2
import aktual.about.data.ArtifactDetail
import aktual.about.data.LicensesLoadState.Failure
import aktual.about.data.LicensesLoadState.Success
import aktual.about.data.LicensesRepository
import aktual.about.vm.SearchLicensesState.Results
import aktual.test.assertThatNextEmissionIsEqualTo
import alakazam.test.standardDispatcher
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SearchLicensesViewModelTest {
  private val repository = mockk<LicensesRepository>()

  private fun TestScope.buildViewModel(
    artifacts: List<ArtifactDetail> = listOf(MANGO, KIWI, PLUM),
  ): SearchLicensesViewModel {
    Dispatchers.setMain(standardDispatcher)
    coEvery { repository.loadLicenses() } returns Success(artifacts)
    return SearchLicensesViewModel(repository, urlOpener = {})
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
      viewModel.setQuery("definitely not a library")
      assertThatNextEmissionIsEqualTo(NoResults)
    }
  }

  @Test
  fun `Failing to load gives no results`() = runTest {
    Dispatchers.setMain(standardDispatcher)
    coEvery { repository.loadLicenses() } returns Failure("something broke")
    val viewModel = SearchLicensesViewModel(repository, urlOpener = {})

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery("mango")
      assertThatNextEmissionIsEqualTo(NoResults)
    }
  }

  @Test
  fun `Matches names case-insensitively`() = runTest {
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery(" MANGO ")
      assertThatNextEmissionIsEqualTo(Results("MANGO", persistentListOf(MANGO)))
    }
  }

  @Test
  fun `Matches artifact, version and license`() = runTest {
    val viewModel = buildViewModel()

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)

      viewModel.setQuery("com.fruit:kiwi")
      assertThatNextEmissionIsEqualTo(Results("com.fruit:kiwi", persistentListOf(KIWI)))

      viewModel.setQuery("7.8.9")
      assertThatNextEmissionIsEqualTo(Results("7.8.9", persistentListOf(PLUM)))

      viewModel.setQuery("mit license")
      assertThatNextEmissionIsEqualTo(Results("mit license", persistentListOf(KIWI)))
    }
  }

  @Test
  fun `Name matches come first`() = runTest {
    val byGroup = MANGO.copy(groupId = "com.plum", artifactId = "other", name = "Other")
    val viewModel = buildViewModel(listOf(byGroup, PLUM))

    viewModel.state.test {
      assertThatNextEmissionIsEqualTo(NoQuery)
      viewModel.setQuery("plum")
      assertThatNextEmissionIsEqualTo(Results("plum", persistentListOf(PLUM, byGroup)))
    }
  }

  private companion object {
    val MANGO =
      ArtifactDetail(
        groupId = "com.fruit",
        artifactId = "mango",
        name = "Mango",
        spdxLicenses = setOf(Apache2),
        version = "1.2.3",
      )

    val KIWI =
      MANGO.copy(
        artifactId = "kiwi",
        name = null,
        spdxLicenses = setOf(Apache2.copy(identifier = "MIT", name = "MIT License")),
      )

    val PLUM = MANGO.copy(artifactId = "plum", name = "Plum", version = "7.8.9")
  }
}
