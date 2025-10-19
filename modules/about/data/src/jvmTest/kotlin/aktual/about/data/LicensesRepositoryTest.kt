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
package aktual.about.data

import aktual.core.model.Assets
import alakazam.test.core.TestCoroutineContexts
import alakazam.test.core.getResourceAsStream
import alakazam.test.core.standardDispatcher
import assertk.assertThat
import assertk.assertions.isDataClassEqualTo
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class LicensesRepositoryTest {
  private lateinit var licensesRepository: LicensesRepository
  private lateinit var assets: Assets

  @Test
  fun `Parse valid JSON`() = runTest {
    // Given
    buildRepo(assetResource = "valid.json")

    // When
    val state = licensesRepository.loadLicenses()

    // Then
    val alakazamAndroidCore = ArtifactDetail(
      groupId = "dev.jonpoulton.alakazam",
      artifactId = "android-core",
      version = "6.0.0",
      name = "Alakazam Android Core",
      spdxLicenses = setOf(Apache2),
      scm = ArtifactScm(url = "https://github.com/jonapoul/alakazam"),
    )

    val composeMaterialRipple = ArtifactDetail(
      groupId = "androidx.compose.material",
      artifactId = "material-ripple",
      version = "1.7.8",
      name = "Compose Material Ripple",
      spdxLicenses = setOf(Apache2),
      scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
    )

    val fragmentKtx = ArtifactDetail(
      groupId = "androidx.fragment",
      artifactId = "fragment-ktx",
      version = "1.8.6",
      name = "Fragment Kotlin Extensions",
      spdxLicenses = setOf(Apache2),
      scm = ArtifactScm(url = "https://cs.android.com/androidx/platform/frameworks/support"),
    )

    val slf4jApi = ArtifactDetail(
      groupId = "org.slf4j",
      artifactId = "slf4j-api",
      version = "2.0.17",
      name = "SLF4J API Module",
      unknownLicenses = setOf(UnknownLicense(name = "MIT", url = "https://opensource.org/license/mit")),
      scm = ArtifactScm(url = "https://github.com/qos-ch/slf4j/slf4j-parent"),
    )

    assertThat(state).isDataClassEqualTo(
      LicensesLoadState.Success(
        libraries = listOf(composeMaterialRipple, fragmentKtx, alakazamAndroidCore, slf4jApi),
      ),
    )
  }

  private fun TestScope.buildRepo(assetResource: String) {
    assets = Assets { getResourceAsStream(assetResource) }
    licensesRepository = LicensesRepository(
      assets = assets,
      contexts = TestCoroutineContexts(standardDispatcher),
    )
  }
}
