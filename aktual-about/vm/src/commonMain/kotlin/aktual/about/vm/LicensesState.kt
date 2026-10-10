package aktual.about.vm

import aktual.about.data.ArtifactDetail
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Immutable
sealed interface LicensesState {
  data object Loading : LicensesState

  data class Loaded(
    val artifacts: ImmutableList<ArtifactDetail>,
    val filterText: String,
    val isSearchActive: Boolean,
  ) : LicensesState

  data object NoneFound : LicensesState

  data class Error(val errorMessage: String) : LicensesState
}

enum class LicenseSorting {
  ByArtifact,
  ByName,
  ByLicense,
}

internal fun LicensesState.Loaded.sortedBy(sorting: LicenseSorting) =
  when (sorting) {
    // LicensesRepository already sorts by artifact
    ByArtifact -> this
    ByName -> copy(artifacts = artifacts.sortedBy { it.sortName() }.toImmutableList())
    ByLicense ->
      copy(
        artifacts =
          artifacts
            .sortedWith(compareBy(nullsLast()) { it.licenseName()?.lowercase() })
            .toImmutableList(),
      )
  }

private fun ArtifactDetail.sortName(): String = (name ?: artifactId).lowercase()

private fun ArtifactDetail.licenseName(): String? =
  spdxLicenses.firstOrNull()?.name ?: unknownLicenses.firstOrNull()?.name

internal fun LicensesState.Loaded.filteredBy(text: String, isSearchActive: Boolean) =
  copy(
    artifacts = artifacts.filter { lib -> lib.matches(text) }.toImmutableList(),
    filterText = text,
    isSearchActive = isSearchActive,
  )

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
