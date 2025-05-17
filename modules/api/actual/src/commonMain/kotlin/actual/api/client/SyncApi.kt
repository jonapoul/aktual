package actual.api.client

import actual.account.model.LoginToken
import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.GetUserFileInfoResponse
import actual.api.model.sync.GetUserKeyRequest
import actual.api.model.sync.GetUserKeyResponse
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.BudgetId
import actual.codegen.Body
import actual.codegen.GET
import actual.codegen.Header
import actual.codegen.KtorApi
import actual.codegen.POST
import actual.url.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface SyncApi {
  @GET("/sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): ListUserFilesResponse.Success

  @GET("/sync/get-user-file-info")
  suspend fun fetchUserFileInfo(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Header(ActualHeaders.FILE_ID) budgetId: BudgetId,
  ): GetUserFileInfoResponse.Success

  @POST("/sync/user-get-key")
  suspend fun fetchUserKey(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Body body: GetUserKeyRequest,
  ): GetUserKeyResponse.Success
}

expect fun SyncApi(serverUrl: ServerUrl, client: HttpClient): SyncApi
