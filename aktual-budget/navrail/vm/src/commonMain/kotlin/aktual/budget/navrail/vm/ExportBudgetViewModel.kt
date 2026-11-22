package aktual.budget.navrail.vm

import aktual.budget.model.DbMetadataKey.Companion.EncryptKeyId
import aktual.budget.utils.BudgetExporter
import aktual.budget.utils.BudgetFiles
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import alakazam.kotlin.requireMessage
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import java.io.OutputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okio.buffer

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ExportBudgetViewModel
internal constructor(
  private val exporter: BudgetExporter,
  private val files: BudgetFiles,
  localPreferences: BudgetLocalPreferences,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val isEncrypted: Boolean = localPreferences[EncryptKeyId] != null

  private val mutableState = MutableStateFlow<ExportBudgetState>(Idle)
  val state: StateFlow<ExportBudgetState> = mutableState.asStateFlow()

  private val readyChannel = Channel<ReadyBudgetExport>(BUFFERED)
  val exports: Flow<ReadyBudgetExport> = readyChannel.receiveAsFlow()

  private var exportJob: Job? = null

  fun export(target: ExportBudgetTarget) {
    if (mutableState.value is Exporting) return
    mutableState.update { Exporting }
    exportJob = viewModelScope.launch {
      runCatchingCancellable(
        attempt = {
          val export = exporter.export()
          val ready =
            ReadyBudgetExport(fileName = export.fileName, target = target) { output ->
              // The zip is never encrypted, so don't leave it lying around
              try {
                files.fileSystem.source(export.path).buffer().inputStream().use {
                  it.copyTo(output)
                }
              } finally {
                files.fileSystem.delete(export.path, mustExist = false)
              }
            }
          readyChannel.send(ready)
          mutableState.update { Idle }
        },
        onFailure = { e ->
          log.e(e) { "Failed exporting budget" }
          mutableState.update { ExportBudgetState.Failed(e.requireMessage()) }
        },
      )
    }
  }

  fun reset() {
    exportJob?.cancel()
    exportJob = null
    mutableState.update { Idle }
  }
}

@Immutable
sealed interface ExportBudgetState {
  data object Idle : ExportBudgetState

  data object Exporting : ExportBudgetState

  @JvmInline value class Failed(val message: String) : ExportBudgetState
}

enum class ExportBudgetTarget {
  Share,
  Save,
}

/** [write] copies the zip into a destination the platform picked. */
@Immutable
data class ReadyBudgetExport(
  val fileName: String,
  val target: ExportBudgetTarget,
  val write: (OutputStream) -> Unit,
)
