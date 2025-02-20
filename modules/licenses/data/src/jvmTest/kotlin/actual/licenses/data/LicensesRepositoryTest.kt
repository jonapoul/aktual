package actual.licenses.data

import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.getResourceAsStream
import alakazam.test.core.standardDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LicensesRepositoryTest {
  private lateinit var licensesRepository: LicensesRepository
  private lateinit var assetsProvider: AssetsProvider

  @Test
  fun `Parse valid JSON`() = runTest {
    // Given
    buildRepo(assetResource = "valid.json")

    // When
    val state = licensesRepository.loadLicenses()

    // Then
    val alakazamAndroidCore = LibraryModel(
      project = "Alakazam Android Core",
      description = "A set of useful functions and extensions for Android development.",
      version = "4.5.0",
      developers = listOf("Jon Poulton"),
      url = "https://github.com/jonapoul/alakazam",
      year = 2021,
      licenses = listOf(LicenseModel.Apache2),
      dependency = "dev.jonpoulton.alakazam:android-core",
    )
    val composeMaterialRipple = LibraryModel(
      project = "Compose Material Ripple",
      description = "Material ripple used to build interactive components",
      version = "1.6.4",
      developers = listOf("AOSP"),
      url = "https://developer.android.com/jetpack/androidx/releases/compose-material#1.6.4",
      year = 2020,
      licenses = listOf(LicenseModel.Apache2),
      dependency = "androidx.compose.material:material-ripple-android",
    )
    val fragmentKtx = LibraryModel(
      project = "Fragment Kotlin Extensions",
      description = "Kotlin extensions for 'fragment' artifact",
      version = "1.6.2",
      developers = listOf("AOSP"),
      url = "https://developer.android.com/jetpack/androidx/releases/fragment#1.6.2",
      year = 2018,
      licenses = listOf(LicenseModel.Apache2),
      dependency = "androidx.fragment:fragment-ktx",
    )
    val voyagerScreenModel = LibraryModel(
      project = "VoyagerScreenModel",
      description = "A pragmatic navigation library for Jetpack Compose",
      version = "1.1.0-alpha04",
      developers = listOf("Adriel Cafe"),
      url = "https://github.com/adrielcafe/voyager",
      year = 2021,
      licenses = listOf(LicenseModel.MIT),
      dependency = "cafe.adriel.voyager:voyager-screenmodel-android",
    )

    assertEquals(
      actual = state,
      expected = LicensesLoadState.Success(
        libraries = listOf(alakazamAndroidCore, composeMaterialRipple, fragmentKtx, voyagerScreenModel),
      ),
    )
  }

  @Test
  fun `Fail for invalid JSON`() = runTest {
    // Given a file without any licenses on the single library
    buildRepo(assetResource = "invalid.json")

    // When
    val state = licensesRepository.loadLicenses()

    // Then
    assertIs<LicensesLoadState.Failure>(state)
  }

  private fun TestScope.buildRepo(assetResource: String) {
    assetsProvider = AssetsProvider { getResourceAsStream(assetResource) }
    licensesRepository = LicensesRepository(
      assetsProvider = assetsProvider,
      contexts = TestCoroutineContexts(standardDispatcher),
    )
  }
}
