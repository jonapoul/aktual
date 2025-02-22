package actual.api.client

import actual.account.model.LoginToken
import actual.api.core.RetrofitResponse
import actual.api.core.adapted
import actual.api.model.account.BootstrapRequest
import actual.api.model.account.BootstrapResponse
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import actual.api.model.account.LoginRequest
import actual.api.model.account.LoginResponse
import actual.api.model.account.NeedsBootstrapResponse
import actual.api.model.internal.ActualHeaders
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AccountApi {
  @GET("account/needs-bootstrap")
  suspend fun needsBootstrap(): RetrofitResponse<NeedsBootstrapResponse.Success>

  @POST("account/bootstrap")
  suspend fun bootstrap(
    @Body body: BootstrapRequest,
  ): RetrofitResponse<BootstrapResponse.Success>

  @POST("account/login")
  suspend fun login(
    @Body body: LoginRequest,
  ): RetrofitResponse<LoginResponse.Success>

  @POST("account/change-password")
  suspend fun changePassword(
    @Body body: ChangePasswordRequest,
    @Header(ActualHeaders.TOKEN) token: LoginToken = body.token,
  ): RetrofitResponse<ChangePasswordResponse.Success>
}

suspend fun AccountApi.needsBoostrapAdapted() =
  needsBootstrap().adapted(ActualJson, NeedsBootstrapResponse.Failure.serializer())

suspend fun AccountApi.bootstrapAdapted(body: BootstrapRequest) =
  bootstrap(body).adapted(ActualJson, BootstrapResponse.Failure.serializer())

suspend fun AccountApi.loginAdapted(body: LoginRequest) =
  login(body).adapted(ActualJson, LoginResponse.Failure.serializer())

suspend fun AccountApi.changePasswordAdapted(body: ChangePasswordRequest, token: LoginToken = body.token) =
  changePassword(body, token).adapted(ActualJson, ChangePasswordResponse.Failure.serializer())
