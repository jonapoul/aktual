package aktual.about.vm

import aktual.about.data.Apache2
import aktual.about.data.ArtifactDetail
import aktual.about.data.ArtifactScm
import aktual.about.data.LicensesLoadState.Failure
import aktual.about.data.LicensesLoadState.Success
import aktual.about.data.LicensesRepository
import aktual.about.vm.LicensesState.Error
import aktual.about.vm.LicensesState.Loaded
import aktual.core.UrlOpener
import aktual.test.assertThatNextEmissionIsEqualTo
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LicensesViewModelTest {
  // real
  private lateinit var viewModel: LicensesViewModel

  // mock
  private lateinit var repository: LicensesRepository
  private lateinit var urlOpener: UrlOpener

  @BeforeTest
  fun before() {
    repository = mockk(relaxed = true)
    urlOpener = mockk(relaxed = true)
  }

  @Test
  fun `Reload data after failure`() = runTest {
    // Given the repository data access fails
    val message = "something broke"
    coEvery { repository.loadLicenses() } returns Failure(message)

    // When
    buildViewModel()

    viewModel.licensesState.test {
      // Then an error state is returned
      assertThatNextEmissionIsEqualTo(Error(message))

      // Given the repo now fetches successfully
      coEvery { repository.loadLicenses() } returns Success(listOf(EXAMPLE_MODEL))

      // When
      viewModel.load()

      // Then a success state is returned
      assertThatNextEmissionIsEqualTo(Loading)
      assertLoaded(EXAMPLE_MODEL)
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Handle empty licenses list`() = runTest {
    // Given the repo now fetches successfully, but nothing is in the list
    coEvery { repository.loadLicenses() } returns Success(emptyList())

    // When
    buildViewModel()

    viewModel.licensesState.test {
      // Then
      assertThatNextEmissionIsEqualTo(NoneFound)
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Open URL`() = runTest {
    // When
    val url = "www.website.com/whatever"
    buildViewModel()
    viewModel.openUrl(url)

    // Then
    verify(exactly = 1) { urlOpener(url) }
    confirmVerified(urlOpener)
  }

  @Test
  fun `Sort licenses`() = runTest {
    // Given libraries in their default order
    val apple = EXAMPLE_MODEL.copy(artifactId = "a", name = "apple", spdxLicenses = setOf(MIT))
    val zebra = EXAMPLE_MODEL.copy(artifactId = "b", name = "Zebra")
    val unnamed = EXAMPLE_MODEL.copy(artifactId = "mango", name = null, spdxLicenses = emptySet())
    val models = listOf(apple, zebra, unnamed)
    coEvery { repository.loadLicenses() } returns Success(models)

    buildViewModel()

    viewModel.licensesState.test {
      assertLoaded(models)

      viewModel.setSorting(ByName)
      assertLoaded(apple, unnamed, zebra)

      // Artifacts without a license go last
      viewModel.setSorting(ByLicense)
      assertLoaded(zebra, apple, unnamed)

      viewModel.setSorting(ByArtifact)
      assertLoaded(models)

      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun buildViewModel() {
    viewModel = LicensesViewModel(licensesRepository = repository, urlOpener = urlOpener)
  }

  private suspend fun TurbineTestContext<LicensesState>.assertLoaded(
    vararg models: ArtifactDetail,
  ) {
    assertLoaded(models.toList())
  }

  private suspend fun TurbineTestContext<LicensesState>.assertLoaded(models: List<ArtifactDetail>) {
    assertThatNextEmissionIsEqualTo(Loaded(models.toImmutableList()))
  }

  private companion object {
    val MIT = Apache2.copy(identifier = "MIT", name = "MIT License")

    val EXAMPLE_MODEL =
      ArtifactDetail(
        groupId = "com.website",
        artifactId = "something",
        name = "Something",
        spdxLicenses = setOf(Apache2),
        scm = ArtifactScm("www.website.com"),
        version = "1.2.3",
      )
  }
}
