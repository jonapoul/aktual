package actual.api.client

import actual.account.model.LoginToken
import actual.api.model.account.BootstrapRequest
import actual.api.model.account.BootstrapResponse
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import actual.api.model.account.LoginRequest
import actual.api.model.account.LoginResponse
import actual.api.model.account.NeedsBootstrapResponse
import actual.api.model.account.ValidateResponse
import actual.api.model.internal.ActualHeaders
import actual.codegen.AdaptedApi
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

@AdaptedApi
interface AccountApi {
  @GET("account/needs-bootstrap")
  suspend fun needsBootstrap(): Response<NeedsBootstrapResponse.Success>

  @POST("account/bootstrap")
  suspend fun bootstrap(
    @Body body: BootstrapRequest,
  ): Response<BootstrapResponse.Success>

  @POST("account/login")
  suspend fun login(
    @Body body: LoginRequest,
  ): Response<LoginResponse.Success>

  @POST("account/change-password")
  suspend fun changePassword(
    @Body body: ChangePasswordRequest,
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): Response<ChangePasswordResponse.Success>

  @POST("account/validate")
  suspend fun validate(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): Response<ValidateResponse.Success>
}
