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

enum class LicenseSorting {
  ByArtifact,
  ByName,
  ByLicense,
}

internal fun LicensesState.Loaded.sortedBy(sorting: LicenseSorting) =
  when (sorting) {
    // LicensesRepository already sorts by artifact
    ByArtifact -> this
    ByName ->
      copy(artifacts = artifacts.sortedBy { it.displayName().lowercase() }.toImmutableList())
    ByLicense ->
      copy(
        artifacts =
          artifacts
            .sortedWith(compareBy(nullsLast()) { it.licenseName()?.lowercase() })
            .toImmutableList(),
      )
  }

internal fun ArtifactDetail.displayName(): String = name ?: artifactId

internal fun ArtifactDetail.licenseName(): String? =
  spdxLicenses.firstOrNull()?.name ?: unknownLicenses.firstOrNull()?.name
