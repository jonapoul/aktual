package actual.api.client

import actual.account.model.LoginToken
import actual.api.core.RetrofitResponse
import actual.api.core.adapted
import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.ListUserFilesResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface SyncApi {
  @GET("sync/list-user-files")
  suspend fun fetchUserFiles(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): RetrofitResponse<ListUserFilesResponse.Success>
}

suspend fun SyncApi.fetchUserFilesAdapted(token: LoginToken) =
  fetchUserFiles(token).adapted(ActualJson, ListUserFilesResponse.Failure.serializer())
