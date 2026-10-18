package aktual.about.vm.logs

import aktual.about.data.LogFile
import aktual.core.model.Bytes
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Immutable
sealed interface LogFilesState {
  data object Loading : LogFilesState

  data object Empty : LogFilesState

  data class Loaded(
    val files: ImmutableList<LogFile>,
    val totalSize: Bytes,
    val today: LocalDate,
  ) : LogFilesState
}
