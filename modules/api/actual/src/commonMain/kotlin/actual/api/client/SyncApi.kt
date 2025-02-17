package actual.api.client

import actual.api.model.internal.ActualHeaders
import actual.api.model.sync.ListUserFilesResponse
import actual.login.model.LoginToken
import retrofit2.http.GET
import retrofit2.http.Header

interface SyncApi {
  @GET("sync/list-user-files")
  suspend fun listUserFiles(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): ListUserFilesResponse
}
