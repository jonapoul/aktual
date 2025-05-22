package actual.about.licenses.data

import dev.drewhamilton.poko.Poko

sealed interface LicensesLoadState {
  @Poko class Success(val libraries: List<ArtifactDetail>) : LicensesLoadState
  @Poko class Failure(val cause: String) : LicensesLoadState
}
