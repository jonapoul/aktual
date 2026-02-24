package aktual.api.client

import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.DeleteUserFileResponse
import aktual.api.model.sync.GetUserFileInfoResponse
import aktual.api.model.sync.GetUserKeyRequest
import aktual.api.model.sync.GetUserKeyResponse
import aktual.api.model.sync.ListUserFilesResponse
import aktual.budget.model.BudgetId
import aktual.core.model.Token
import kotlinx.coroutines.flow.Flow
import okio.Path

interface SyncApi {
  fun downloadUserFile(token: Token, budgetId: BudgetId, path: Path): Flow<SyncDownloadState>

  suspend fun fetchUserFiles(token: Token): ListUserFilesResponse.Success

  suspend fun fetchUserFileInfo(token: Token, budgetId: BudgetId): GetUserFileInfoResponse.Success

  suspend fun fetchUserKey(body: GetUserKeyRequest): GetUserKeyResponse.Success

  suspend fun delete(body: DeleteUserFileRequest): DeleteUserFileResponse.Success
}

sealed interface SyncDownloadState {
  data class InProgress(val bytesSentTotal: Long, val contentLength: Long) : SyncDownloadState

  data class Done(val path: Path, val contentLength: Long) : SyncDownloadState
}
