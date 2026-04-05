@file:Suppress("StringLiteralDuplication")

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
import aktual.core.model.Password
import aktual.core.model.ServerUrl
import aktual.core.model.Token
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path

class AccountApiImpl(private val client: HttpClient, private val serverUrl: ServerUrl) :
  AccountApi {
  private val urlProtocol = serverUrl.protocol()

  override suspend fun needsBootstrap(): NeedsBootstrapResponse.Success =
    client
      .get {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/account/needs-bootstrap")
        }
      }
      .body<NeedsBootstrapResponse.Success>()

  override suspend fun loginMethods(): LoginMethodsResponse.Success =
    client
      .get {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/account/login-methods")
        }
      }
      .body<LoginMethodsResponse.Success>()

  override suspend fun bootstrap(body: BootstrapRequest): BootstrapResponse.Success =
    client
      .post {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/account/bootstrap")
        }
        contentType(ContentType.Application.Json)
        setBody(body)
      }
      .body<BootstrapResponse.Success>()

  override suspend fun login(body: LoginRequest.Password): LoginResponse =
    try {
      client
        .post {
          url {
            protocol = urlProtocol
            host = serverUrl.baseUrl
            path("/account/login")
          }
          contentType(ContentType.Application.Json)
          setBody(body)
        }
        .body<LoginResponse.Success>()
    } catch (e: ResponseException) {
      e.response.body<LoginResponse.Failure>()
    }

  override suspend fun login(body: LoginRequest.OpenId): LoginResponse =
    try {
      client
        .post {
          url {
            protocol = urlProtocol
            host = serverUrl.baseUrl
            path("/account/login")
          }
          contentType(ContentType.Application.Json)
          setBody(body)
        }
        .body<LoginResponse.Success>()
    } catch (e: ResponseException) {
      e.response.body<LoginResponse.Failure>()
    }

  override suspend fun login(body: LoginRequest.Header, password: Password): LoginResponse =
    try {
      client
        .post {
          url {
            protocol = urlProtocol
            host = serverUrl.baseUrl
            path("/account/login")
          }
          header(AktualHeaders.PASSWORD, password)
          contentType(ContentType.Application.Json)
          setBody(body)
        }
        .body<LoginResponse.Success>()
    } catch (e: ResponseException) {
      e.response.body<LoginResponse.Failure>()
    }

  override suspend fun changePassword(
    body: ChangePasswordRequest,
    token: Token,
  ): ChangePasswordResponse.Success =
    client
      .post {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/account/change-password")
        }
        header(AktualHeaders.TOKEN, token)
        contentType(ContentType.Application.Json)
        setBody(body)
      }
      .body<ChangePasswordResponse.Success>()

  override suspend fun validate(token: Token): ValidateResponse.Success =
    client
      .post {
        url {
          protocol = urlProtocol
          host = serverUrl.baseUrl
          path("/account/validate")
        }
        header(AktualHeaders.TOKEN, token)
      }
      .body<ValidateResponse.Success>()
}
