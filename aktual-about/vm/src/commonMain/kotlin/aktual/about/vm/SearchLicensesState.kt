package aktual.about.vm

import aktual.about.model.ArtifactDetail
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchLicensesState {
  data object NoQuery : SearchLicensesState

  data object NoResults : SearchLicensesState

  data class Results(val query: String, val artifacts: ImmutableList<ArtifactDetail>) :
    SearchLicensesState
}
