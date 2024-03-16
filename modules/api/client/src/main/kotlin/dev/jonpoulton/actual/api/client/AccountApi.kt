package dev.jonpoulton.actual.api.client

import dev.jonpoulton.actual.api.model.account.BootstrapRequest
import dev.jonpoulton.actual.api.model.account.BootstrapResponse
import dev.jonpoulton.actual.api.model.account.NeedsBootstrapResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AccountApi {
  @GET("account/needs-bootstrap")
  suspend fun needsBootstrap(): NeedsBootstrapResponse

  @POST("account/bootstrap")
  suspend fun bootstrap(@Body body: BootstrapRequest): BootstrapResponse
}
