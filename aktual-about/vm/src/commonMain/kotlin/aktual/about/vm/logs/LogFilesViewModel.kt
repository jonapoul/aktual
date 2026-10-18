package aktual.about.vm.logs

import aktual.about.data.LogsRepository
import aktual.core.model.Bytes
import aktual.core.model.bytes
import aktual.di.AppScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class LogFilesViewModel
internal constructor(
  private val repository: LogsRepository,
  private val exporter: LogExporter,
) : ViewModel() {
  private val mutableState = MutableStateFlow<LogFilesState>(Loading)
  val state: StateFlow<LogFilesState> = mutableState.asStateFlow()

  private val exportChannel = Channel<LogExport>(BUFFERED)
  val exports: Flow<LogExport> = exportChannel.receiveAsFlow()

  init {
    load()
  }

  fun load() {
    viewModelScope.launch {
      val files = repository.files()
      mutableState.update {
        if (files.isEmpty()) {
          Empty
        } else {
          val totalSize =
            files.fold(Bytes.Zero) { acc, f -> (acc.numBytes + f.size.numBytes).bytes }
          LogFilesState.Loaded(files.toImmutableList(), totalSize, repository.today())
        }
      }
    }
  }

  fun deleteAll() {
    viewModelScope.launch {
      logcat.i { "Deleting all log files" }
      repository.deleteAll()
      load()
    }
  }

  fun delete(date: LocalDate) {
    viewModelScope.launch {
      logcat.i { "Deleting log file for $date" }
      repository.delete(date)
      load()
    }
  }

  fun export(scope: LogExportScope, target: LogExportTarget) {
    viewModelScope.launch { exportChannel.send(exporter.export(scope, target)) }
  }
}
