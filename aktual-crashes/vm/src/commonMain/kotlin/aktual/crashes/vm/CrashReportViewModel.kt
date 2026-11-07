package aktual.crashes.vm

import aktual.about.data.LogExport
import aktual.about.data.LogExportTarget
import aktual.about.data.LogsRepository
import aktual.core.model.CrashFile
import aktual.crashes.data.CrashIssueUrlBuilder
import aktual.crashes.data.CrashesRepository
import aktual.di.AppScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@Stable
@AssistedInject
class CrashReportViewModel
internal constructor(
  @Assisted private val file: CrashFile,
  private val repository: CrashesRepository,
  private val logsRepository: LogsRepository,
  private val issueUrlBuilder: CrashIssueUrlBuilder,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  private val mutableState = MutableStateFlow<CrashReportState>(Loading)
  val state: StateFlow<CrashReportState> = mutableState.asStateFlow()

  private val exportChannel = Channel<LogExport>(BUFFERED)
  val exports: Flow<LogExport> = exportChannel.receiveAsFlow()

  private val deletedChannel = Channel<Unit>(BUFFERED)
  val deleted: Flow<Unit> = deletedChannel.receiveAsFlow()

  init {
    viewModelScope.launch {
      val report = repository.report(file)
      mutableState.update {
        if (report == null) {
          Missing
        } else {
          CrashReportState.Loaded(report, issueUrlBuilder.build(report))
        }
      }
    }
  }

  fun export(includeLogs: Boolean, target: LogExportTarget) {
    viewModelScope.launch {
      val report = (state.value as? Loaded)?.report ?: return@launch
      val crash = repository.read(file).orEmpty()
      val text =
        if (includeLogs) {
          crash + "\n" + logsRepository.read(listOf(report.time.date))
        } else {
          crash
        }
      exportChannel.send(LogExport(fileName = "aktual-${file.name}", text = text, target = target))
    }
  }

  fun delete() {
    viewModelScope.launch {
      logcat.i { "Deleting crash report $file" }
      repository.delete(file)
      deletedChannel.send(Unit)
    }
  }

  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  fun interface Factory : ManualViewModelAssistedFactory {
    fun create(@Assisted file: CrashFile): CrashReportViewModel
  }
}
