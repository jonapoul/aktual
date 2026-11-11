package aktual.account.vm

import aktual.api.client.AccountApi
import aktual.api.model.account.FailureReason
import aktual.api.model.account.LoginRequest
import aktual.api.model.account.LoginResponse
import aktual.core.model.AvailableLoginMethod
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.core.utils.runCatchingCancellable
import aktual.di.RunLevelController
import aktual.log
import aktual.prefs.AppPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import java.io.IOException
import kotlinx.coroutines.withContext

@Inject
class LoginRequester
internal constructor(
  private val accountApi: AccountApi,
  private val contexts: CoroutineContexts,
  private val preferences: AppPreferences,
  private val runLevelController: RunLevelController,
) {
  suspend fun fetchLoginMethods(): List<AvailableLoginMethod> =
    runCatchingCancellable(
      attempt = { withContext(contexts.io) { accountApi.loginMethods().methods } },
      onFailure = { e ->
        log.w(e) { "Failed to fetch login methods" }
        emptyList()
      },
    )

  suspend fun logIn(
    password: Password,
    loginMethod: LoginMethod = LoginMethod.Password,
  ): LoginResult {
    val response =
      runCatchingCancellable(
        attempt = {
          withContext(contexts.io) {
            when (loginMethod) {
              LoginMethod.Password -> accountApi.login(LoginRequest.Password(password))
              Header -> accountApi.login(LoginRequest.Header(), password)
              OpenId -> accountApi.login(LoginRequest.OpenId(password, returnUrl = ""))
              ApiKey,
              Unknown -> error("Unsupported login method $loginMethod")
            }
          }
        },
        onFailure = { e ->
          return e.toLoginResult()
        },
      )

    return when (response) {
      is Failure -> response.reason.toLoginResult()
      is Success -> response.data.toLoginResult()
    }
  }

  suspend fun logIn(apiKey: Token): LoginResult {
    val response =
      runCatchingCancellable(
        attempt = { withContext(contexts.io) { accountApi.validate(apiKey) } },
        onFailure = { e ->
          return e.toLoginResult()
        },
      )

    return when (response) {
      is Failure ->
        if (response.reason == Unauthorized) {
          InvalidApiKey
        } else {
          response.reason.toLoginResult()
        }

      is Success -> onValidToken(apiKey)
    }
  }

  private fun Throwable.toLoginResult(): LoginResult.Failure =
    if (this is IOException) {
      LoginResult.NetworkFailure(requireMessage())
    } else {
      LoginResult.OtherFailure(requireMessage())
    }

  private suspend fun onValidToken(token: Token): LoginResult {
    runLevelController.onLoggedIn(token)
    preferences.token.set(token)
    return LoginResult.Success(token)
  }

  private fun FailureReason.toLoginResult(): LoginResult.Failure =
    when (this) {
      TokenExpired -> TokenExpired
      InvalidPassword -> InvalidPassword
      else -> LoginResult.OtherFailure(reason)
    }

  private suspend fun LoginResponse.Data.toLoginResult(): LoginResult =
    when (this) {
      is Invalid -> InvalidPassword

      is Redirect -> LoginResult.Redirect(returnUrl)

      is Valid -> onValidToken(token)
    }
}
