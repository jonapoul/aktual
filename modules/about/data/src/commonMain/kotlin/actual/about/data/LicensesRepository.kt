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
package actual.about.data

import actual.core.model.Assets
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer

@Inject
class LicensesRepository internal constructor(
  private val assets: Assets,
  private val contexts: CoroutineContexts,
) {
  suspend fun loadLicenses(): LicensesLoadState = try {
    val jsonString = withContext(contexts.io) {
      assets
        .getStream(LICENSES_FILENAME)
        .reader()
        .use { it.readText() }
    }

    val dirtyLibraries = withContext(contexts.default) {
      LicensesJson.decodeFromString(
        deserializer = ListSerializer(ArtifactDetail.serializer()),
        string = jsonString,
      )
    }

    val sorted = dirtyLibraries.sortedBy { it.id }
    LicensesLoadState.Success(sorted)
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    LicensesLoadState.Failure(e.requireMessage())
  }

  private companion object {
    const val LICENSES_FILENAME = "app/cash/licensee/artifacts.json"
  }
}
