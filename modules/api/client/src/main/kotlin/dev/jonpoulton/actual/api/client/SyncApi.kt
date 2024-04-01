package dev.jonpoulton.actual.api.client

import dev.jonpoulton.actual.api.model.sync.ListFilesResponse
import dev.jonpoulton.actual.core.model.LoginToken
import retrofit2.http.GET
import retrofit2.http.Header

interface SyncApi {
  @GET("sync/list-user-files")
  suspend fun listFiles(
    @Header(TOKEN_HEADER) token: LoginToken,
  ): ListFilesResponse
}
