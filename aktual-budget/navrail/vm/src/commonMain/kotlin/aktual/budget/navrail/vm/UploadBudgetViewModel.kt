package aktual.budget.navrail.vm

import aktual.budget.utils.BudgetUploader
import aktual.budget.utils.UploadResult
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class UploadBudgetViewModel
internal constructor(
  private val uploader: BudgetUploader,
  server: BudgetServer,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val serverHost: String? = (server as? Remote)?.url?.baseUrl

  private val mutableState = MutableStateFlow<UploadBudgetState>(Idle)
  val state: StateFlow<UploadBudgetState> = mutableState.asStateFlow()

  fun upload() {
    if (mutableState.value is Uploading) return
    mutableState.update { Uploading }
    viewModelScope.launch {
      val newState =
        when (val result = uploader.upload()) {
          Success -> UploadBudgetState.Done
          is Failure -> UploadBudgetState.Failed(result)
        }
      mutableState.update { newState }
    }
  }

  // Leaves a running upload alone, so it finishes even with the dialog closed
  fun reset() = mutableState.update { it as? Uploading ?: Idle }
}

@Immutable
sealed interface UploadBudgetState {
  data object Idle : UploadBudgetState

  data object Uploading : UploadBudgetState

  data object Done : UploadBudgetState

  @JvmInline value class Failed(val failure: UploadResult.Failure) : UploadBudgetState
}
