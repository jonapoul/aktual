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
import aktual.core.model.Token

interface AccountApi {
  suspend fun needsBootstrap(): NeedsBootstrapResponse.Success

  suspend fun loginMethods(): LoginMethodsResponse.Success

  suspend fun bootstrap(body: BootstrapRequest): BootstrapResponse.Success

  suspend fun login(body: LoginRequest.Password): LoginResponse

  suspend fun login(body: LoginRequest.OpenId): LoginResponse

  suspend fun login(body: LoginRequest.Header, password: Password): LoginResponse

  suspend fun changePassword(
    body: ChangePasswordRequest,
    token: Token,
  ): ChangePasswordResponse.Success

  suspend fun validate(token: Token): ValidateResponse.Success
}
