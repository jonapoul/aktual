package actual.api.client

import actual.api.model.account.BootstrapRequest
import actual.api.model.account.BootstrapResponse
import actual.api.model.account.LoginRequest
import actual.api.model.account.LoginResponse
import actual.api.model.account.NeedsBootstrapResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AccountApi {
  @GET("account/needs-bootstrap")
  suspend fun needsBootstrap(): NeedsBootstrapResponse

  @POST("account/bootstrap")
  suspend fun bootstrap(
    @Body body: BootstrapRequest,
  ): BootstrapResponse

  @POST("account/login")
  suspend fun login(
    @Body body: LoginRequest,
  ): LoginResponse
}
