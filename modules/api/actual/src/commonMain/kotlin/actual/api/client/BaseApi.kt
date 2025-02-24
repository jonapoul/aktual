package actual.api.client

import actual.api.model.base.InfoResponse
import retrofit2.Response
import retrofit2.http.GET

interface BaseApi {
  @GET("info")
  suspend fun info(): Response<InfoResponse>
}
