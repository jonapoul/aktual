package aktual.api.client

import aktual.api.model.internal.AktualHeaders
import aktual.api.model.sync.DeleteUserFileRequest
import aktual.api.model.sync.DeleteUserFileResponse
import aktual.api.model.sync.GetUserFileInfoResponse
import aktual.api.model.sync.GetUserKeyRequest
import aktual.api.model.sync.GetUserKeyResponse
import aktual.api.model.sync.ListUserFilesResponse
import aktual.budget.model.BudgetId
import aktual.codegen.Body
import aktual.codegen.GET
import aktual.codegen.Header
import aktual.codegen.KtorApi
import aktual.codegen.POST
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import io.ktor.client.HttpClient

@KtorApi
interface SyncApi {
  @GET("/sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(AktualHeaders.TOKEN) token: Token,
  ): ListUserFilesResponse.Success

  @GET("/sync/get-user-file-info")
  suspend fun fetchUserFileInfo(
    @Header(AktualHeaders.TOKEN) token: Token,
    @Header(AktualHeaders.FILE_ID) budgetId: BudgetId,
  ): GetUserFileInfoResponse.Success

  @POST("/sync/user-get-key")
  suspend fun fetchUserKey(
    @Body body: GetUserKeyRequest,
  ): GetUserKeyResponse.Success

  @POST("/sync/delete-user-file")
  suspend fun delete(
    @Body body: DeleteUserFileRequest,
  ): DeleteUserFileResponse.Success
}

expect fun SyncApi(serverUrl: ServerUrl, client: HttpClient): SyncApi
