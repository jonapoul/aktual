package actual.api.client

import actual.core.model.LoginToken
import actual.core.model.Password
import actual.api.model.account.BootstrapRequest
import actual.api.model.account.BootstrapResponse
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import actual.api.model.account.LoginMethodsResponse
import actual.api.model.account.LoginRequest
import actual.api.model.account.LoginResponse
import actual.api.model.account.NeedsBootstrapResponse
import actual.api.model.account.ValidateResponse
import actual.api.model.internal.ActualHeaders
import actual.codegen.Body
import actual.codegen.GET
import actual.codegen.Header
import actual.codegen.KtorApi
import actual.codegen.POST
import actual.core.model.ServerUrl
import io.ktor.client.HttpClient

@KtorApi
interface AccountApi {
  @GET("/account/needs-bootstrap")
  suspend fun needsBootstrap(): NeedsBootstrapResponse.Success

  @GET("/account/login-methods")
  suspend fun loginMethods(): LoginMethodsResponse.Success

  @POST("/account/bootstrap")
  suspend fun bootstrap(
    @Body body: BootstrapRequest,
  ): BootstrapResponse.Success

  @POST("/account/login")
  suspend fun login(
    @Body body: LoginRequest.Password,
  ): LoginResponse.Success

  @POST("/account/login")
  suspend fun login(
    @Body body: LoginRequest.OpenId,
  ): LoginResponse.Success

  @POST("/account/login")
  suspend fun login(
    @Body body: LoginRequest.Header,
    @Header(ActualHeaders.PASSWORD) password: Password,
  ): LoginResponse.Success

  @POST("/account/change-password")
  suspend fun changePassword(
    @Body body: ChangePasswordRequest,
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): ChangePasswordResponse.Success

  @POST("/account/validate")
  suspend fun validate(
    @Header(ActualHeaders.TOKEN) token: LoginToken,
  ): ValidateResponse.Success
}

expect fun AccountApi(serverUrl: ServerUrl, client: HttpClient): AccountApi
