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
package aktual.about.vm

import aktual.about.data.ArtifactDetail
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
sealed interface LicensesState {
  data object Loading : LicensesState
  data class Loaded(val artifacts: ImmutableList<ArtifactDetail>) : LicensesState
  data object NoneFound : LicensesState
  data class Error(val errorMessage: String) : LicensesState
}

internal fun LicensesState.Loaded.filteredBy(text: String): LicensesState.Loaded {
  val filtered = artifacts.filter { lib -> lib.matches(text) }
  return LicensesState.Loaded(artifacts = filtered.toImmutableList())
}

private fun ArtifactDetail.matches(text: String): Boolean =
  name.contains(text) ||
    groupId.contains(text) ||
    artifactId.contains(text) ||
    version.contains(text) ||
    scm?.url.contains(text) ||
    spdxLicenses.any { it.identifier.contains(text) || it.name.contains(text) || it.url.contains(text) } ||
    unknownLicenses.any { it.name.contains(text) || it.url.contains(text) }

private fun String?.contains(other: String): Boolean = this?.contains(other, ignoreCase = true) == true
