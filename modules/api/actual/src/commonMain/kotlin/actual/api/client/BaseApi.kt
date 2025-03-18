package actual.api.client

import actual.api.model.base.InfoResponse
import actual.codegen.AdaptedApi
import retrofit2.Response
import retrofit2.http.GET

@AdaptedApi
interface BaseApi {
  @GET("info")
  suspend fun info(): Response<InfoResponse>
}
