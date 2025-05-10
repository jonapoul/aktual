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
import actual.url.model.ServerUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body

interface AccountApi {
  suspend fun needsBootstrap(): NeedsBootstrapResponse.Success
  suspend fun bootstrap(body: BootstrapRequest): BootstrapResponse.Success
  suspend fun login(body: LoginRequest): LoginResponse.Success
  suspend fun changePassword(body: ChangePasswordRequest, token: LoginToken): ChangePasswordResponse.Success
  suspend fun validate(token: LoginToken): ValidateResponse.Success
}

fun AccountApi(url: ServerUrl, client: HttpClient): AccountApi = AccountApiClient(url, client)

private class AccountApiClient(private val serverUrl: ServerUrl, private val client: HttpClient) : AccountApi {
  override suspend fun needsBootstrap() = client
    .get(serverUrl, path = "/account/needs-bootstrap")
    .body<NeedsBootstrapResponse.Success>()

  override suspend fun bootstrap(body: BootstrapRequest) = client
    .post(serverUrl, body, path = "/account/bootstrap")
    .body<BootstrapResponse.Success>()

  override suspend fun login(body: LoginRequest) = client
    .post(serverUrl, body, path = "/account/login")
    .body<LoginResponse.Success>()

  override suspend fun changePassword(body: ChangePasswordRequest, token: LoginToken) = client
    .post(serverUrl, body, path = "/account/change-password", headers = mapOf(ActualHeaders.TOKEN to token.value))
    .body<ChangePasswordResponse.Success>()

  override suspend fun validate(token: LoginToken) = client
    .post(serverUrl, path = "/account/validate", headers = mapOf(ActualHeaders.TOKEN to token.value))
    .body<ValidateResponse.Success>()
}
