package aktual.budget.navrail.vm

import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.DbMetadataKey.Companion.EncryptKeyId
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.LastSyncedTimestamp
import aktual.budget.model.SyncState
import aktual.budget.utils.BudgetLocalPreferences
import aktual.budget.utils.BudgetUploader
import aktual.budget.utils.SyncStateHolder
import aktual.budget.utils.UploadResult
import aktual.core.model.BudgetServer
import aktual.di.BudgetScope
import aktual.di.ViewModelContext
import aktual.di.newScope
import aktual.log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Asks what to do once the server says the open budget no longer exists, e.g. after it was deleted
 * from another device. The local copy is never touched, since it may now be the only one left.
 */
@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class MissingBudgetViewModel
internal constructor(
  private val uploader: BudgetUploader,
  private val localPreferences: BudgetLocalPreferences,
  private val syncStateHolder: SyncStateHolder,
  server: BudgetServer,
  viewModelContext: ViewModelContext,
) : ViewModel(viewModelContext.newScope()) {
  val serverHost: String? = (server as? Remote)?.url?.baseUrl

  private val upload = MutableStateFlow<UploadStep>(Idle)

  val state: StateFlow<MissingBudgetState> =
    combine(syncStateHolder, upload, localPreferences.observe(EncryptKeyId)) {
        syncState,
        upload,
        encryptKeyId ->
        state(syncState, upload, isEncrypted = encryptKeyId != null)
      }
      .stateIn(
        viewModelScope,
        Eagerly,
        initialValue =
          state(
            syncState = syncStateHolder.value,
            upload = upload.value,
            isEncrypted = localPreferences[EncryptKeyId] != null,
          ),
      )

  fun reupload() {
    if (upload.value is Uploading || localPreferences[EncryptKeyId] != null) return
    upload.update { Uploading }
    // The server keeps deleted budgets' IDs, so this one can't be reused
    detach()
    viewModelScope.launch {
      val result = uploader.upload()
      log.i { "Uploaded missing budget again: $result" }
      upload.update {
        when (result) {
          Success -> Idle
          is Failure -> UploadStep.Failed(result)
        }
      }
    }
  }

  fun keepLocal() {
    if (upload.value is Uploading) return
    detach()
    upload.update { Idle }
  }

  // Makes the budget local-only, which also clears the sync error
  private fun detach() {
    localPreferences.update { metadata -> metadata - DETACHED_KEYS }
    syncStateHolder.update { Inactive }
  }

  private fun state(
    syncState: SyncState,
    upload: UploadStep,
    isEncrypted: Boolean,
  ): MissingBudgetState =
    when (upload) {
      Uploading -> MissingBudgetState.Shown(canReupload = true, isUploading = true)

      is Failed -> MissingBudgetState.Shown(canReupload = true, failure = upload.failure)

      Idle ->
        if (syncState is FileNotFound) {
          MissingBudgetState.Shown(canReupload = !isEncrypted)
        } else {
          Hidden
        }
    }

  private sealed interface UploadStep {
    data object Idle : UploadStep

    data object Uploading : UploadStep

    data class Failed(val failure: UploadResult.Failure) : UploadStep
  }

  private companion object {
    val DETACHED_KEYS = listOf(CloudFileId, GroupId, LastSyncedTimestamp)
  }
}

@Immutable
sealed interface MissingBudgetState {
  data object Hidden : MissingBudgetState

  data class Shown(
    // Uploading encrypted budgets isn't supported yet
    val canReupload: Boolean,
    val isUploading: Boolean = false,
    val failure: UploadResult.Failure? = null,
  ) : MissingBudgetState
}
