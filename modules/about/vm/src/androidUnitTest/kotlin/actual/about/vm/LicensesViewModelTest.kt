/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actual.about.vm

import actual.about.data.Apache2
import actual.about.data.ArtifactDetail
import actual.about.data.ArtifactScm
import actual.about.data.LicensesLoadState
import actual.about.data.LicensesRepository
import actual.core.model.UrlOpener
import actual.test.assertThatNextEmissionIsEqualTo
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test

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
    coEvery { repository.loadLicenses() } returns LicensesLoadState.Failure(message)

    // When
    buildViewModel()

    viewModel.licensesState.test {
      // Then an error state is returned
      assertThatNextEmissionIsEqualTo(LicensesState.Error(message))

      // Given the repo now fetches successfully
      coEvery { repository.loadLicenses() } returns LicensesLoadState.Success(listOf(EXAMPLE_MODEL))

      // When
      viewModel.load()

      // Then a success state is returned
      assertThatNextEmissionIsEqualTo(LicensesState.Loading)
      assertLoaded(EXAMPLE_MODEL)
      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Handle empty licenses list`() = runTest {
    // Given the repo now fetches successfully, but nothing is in the list
    coEvery { repository.loadLicenses() } returns LicensesLoadState.Success(emptyList())

    // When
    buildViewModel()

    viewModel.licensesState.test {
      // Then
      assertThatNextEmissionIsEqualTo(LicensesState.NoneFound)
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
  fun `Toggle search bar and enter text`() = runTest {
    // Given the repo fetches a library successfully
    val models = listOf(EXAMPLE_MODEL)
    coEvery { repository.loadLicenses() } returns LicensesLoadState.Success(models)

    // When
    buildViewModel()

    // Then
    viewModel.searchBarState.test {
      assertThatNextEmissionIsEqualTo(SearchBarState.Gone)

      viewModel.toggleSearchBar()
      assertThatNextEmissionIsEqualTo(SearchBarState.Visible(text = ""))

      viewModel.setSearchText(text = "Hello world")
      assertThatNextEmissionIsEqualTo(SearchBarState.Visible(text = "Hello world"))

      viewModel.toggleSearchBar()
      assertThatNextEmissionIsEqualTo(SearchBarState.Gone)

      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Filter licenses based on search results`() = runTest {
    // Given the repo fetches some libraries successfully
    val basicLib = EXAMPLE_MODEL
    val projectLib = EXAMPLE_MODEL.copy(name = "my project")
    val versionLib = EXAMPLE_MODEL.copy(version = "7.8.9")
    val urlLib = EXAMPLE_MODEL.copy(scm = ArtifactScm("www.url.com"))
    val licenseLib = EXAMPLE_MODEL.copy(spdxLicenses = setOf(Apache2.copy(identifier = "MIT")))
    val allLibraries = listOf(basicLib, projectLib, versionLib, urlLib, licenseLib)
    coEvery { repository.loadLicenses() } returns LicensesLoadState.Success(allLibraries)

    buildViewModel()

    viewModel.licensesState.test {
      // No filter
      assertLoaded(allLibraries)

      // Apply filters
      viewModel.toggleSearchBar()
      viewModel.setSearchText(text = "my project")
      assertLoaded(projectLib)

      viewModel.setSearchText(text = "url")
      assertLoaded(urlLib)

      viewModel.setSearchText(text = "MIT")
      assertLoaded(licenseLib)

      viewModel.setSearchText(text = "")
      assertLoaded(allLibraries)

      cancelAndIgnoreRemainingEvents()
    }
  }

  private fun buildViewModel() {
    viewModel = LicensesViewModel(
      licensesRepository = repository,
      urlOpener = urlOpener,
    )
  }

  private suspend fun TurbineTestContext<LicensesState>.assertLoaded(vararg models: ArtifactDetail) {
    assertLoaded(models.toList())
  }

  private suspend fun TurbineTestContext<LicensesState>.assertLoaded(models: List<ArtifactDetail>) {
    assertThatNextEmissionIsEqualTo(LicensesState.Loaded(models.toImmutableList()))
  }

  private companion object {
    val EXAMPLE_MODEL = ArtifactDetail(
      groupId = "com.website",
      artifactId = "something",
      name = "Something",
      spdxLicenses = setOf(Apache2),
      scm = ArtifactScm("www.website.com"),
      version = "1.2.3",
    )
  }
}
