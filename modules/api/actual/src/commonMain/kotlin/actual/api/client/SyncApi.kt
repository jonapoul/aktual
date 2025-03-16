package actual.api.client

import actual.account.model.LoginToken
import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.GetUserKeyRequest
import actual.api.model.sync.GetUserKeyResponse
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.BudgetId
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface SyncApi {
  @GET("sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): Response<ListUserFilesResponse.Success>

  @Streaming
  @GET("sync/download-user-file")
  suspend fun downloadUserFile(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Header(ActualHeaders.FILE_ID) budgetId: BudgetId,
  ): Response<ResponseBody>

  @GET("sync/get-user-file-info")
  suspend fun fetchUserFileInfo(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Header(ActualHeaders.FILE_ID) budgetId: BudgetId,
  ): Response<ResponseBody> // TODO

  @POST("sync/user-get-key")
  suspend fun fetchUserKey(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Body body: GetUserKeyRequest,
  ): Response<GetUserKeyResponse.Success>
}
