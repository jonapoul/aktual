package actual.api.client

import actual.account.model.LoginToken
import actual.api.core.RetrofitResponse
import actual.api.core.adapted
import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.BudgetId
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Streaming

interface SyncApi {
  @GET("sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): RetrofitResponse<ListUserFilesResponse.Success>

  @Streaming
  @GET("sync/download-user-file")
  suspend fun downloadUserFile(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Header(ActualHeaders.FILE_ID) budgetId: BudgetId,
  ): RetrofitResponse<ResponseBody>

  @GET("sync/get-user-file-info")
  suspend fun fetchUserFileInfo(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
    @Header(ActualHeaders.FILE_ID) budgetId: BudgetId,
  ): RetrofitResponse<ResponseBody> // TODO
}

suspend fun SyncApi.fetchUserFilesAdapted(token: LoginToken) =
  fetchUserFiles(token).adapted(ActualJson, ListUserFilesResponse.Failure.serializer())
