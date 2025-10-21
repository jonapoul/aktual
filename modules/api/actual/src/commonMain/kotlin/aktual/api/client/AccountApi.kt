/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.api.client

import aktual.api.model.account.BootstrapRequest
import aktual.api.model.account.BootstrapResponse
import aktual.api.model.account.ChangePasswordRequest
import aktual.api.model.account.ChangePasswordResponse
import aktual.api.model.account.LoginMethodsResponse
import aktual.api.model.account.LoginRequest
import aktual.api.model.account.LoginResponse
import aktual.api.model.account.NeedsBootstrapResponse
import aktual.api.model.account.ValidateResponse
import aktual.api.model.internal.AktualHeaders
import aktual.codegen.Body
import aktual.codegen.GET
import aktual.codegen.Header
import aktual.codegen.KtorApi
import aktual.codegen.POST
import aktual.core.model.LoginToken
import aktual.core.model.Password
import aktual.core.model.ServerUrl
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
    @Header(AktualHeaders.PASSWORD) password: Password,
  ): LoginResponse.Success

  @POST("/account/change-password")
  suspend fun changePassword(
    @Body body: ChangePasswordRequest,
    @Header(AktualHeaders.TOKEN) token: LoginToken,
  ): ChangePasswordResponse.Success

  @POST("/account/validate")
  suspend fun validate(
    @Header(AktualHeaders.TOKEN) token: LoginToken,
  ): ValidateResponse.Success
}

expect fun AccountApi(serverUrl: ServerUrl, client: HttpClient): AccountApi
