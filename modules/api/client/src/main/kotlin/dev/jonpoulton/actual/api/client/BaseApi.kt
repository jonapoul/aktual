package dev.jonpoulton.actual.api.client

import dev.jonpoulton.actual.api.model.base.InfoResponse
import retrofit2.http.GET

interface BaseApi {
  @GET("info")
  suspend fun info(): InfoResponse
}
