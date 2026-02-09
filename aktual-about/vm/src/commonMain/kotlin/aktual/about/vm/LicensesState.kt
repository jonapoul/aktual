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
        spdxLicenses.any {
          it.identifier.contains(text) || it.name.contains(text) || it.url.contains(text)
        } ||
        unknownLicenses.any { it.name.contains(text) || it.url.contains(text) }

private fun String?.contains(other: String): Boolean =
    this?.contains(other, ignoreCase = true) == true
