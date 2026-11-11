package aktual.account.vm

import aktual.api.client.AccountApi
import aktual.api.model.account.FailureReason
import aktual.api.model.account.LoginRequest
import aktual.api.model.account.LoginResponse
import aktual.core.model.AvailableLoginMethod
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.core.utils.OpenIdRedirects
import aktual.core.utils.runCatchingCancellable
import aktual.di.RunLevelController
import aktual.log
import aktual.prefs.AppPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Inject
class LoginRequester
internal constructor(
  private val accountApi: AccountApi,
  private val contexts: CoroutineContexts,
  private val preferences: AppPreferences,
  private val runLevelController: RunLevelController,
  private val openIdRedirects: OpenIdRedirects,
) {
  // Session tokens the server sends back once an OpenID sign-in finishes
  val openIdTokens: Flow<Token>
    get() = openIdRedirects.tokens

  suspend fun fetchLoginMethods(): List<AvailableLoginMethod> =
    runCatchingCancellable(
      attempt = { withContext(contexts.io) { accountApi.loginMethods().methods } },
      onFailure = { e ->
        log.w(e) { "Failed to fetch login methods" }
        emptyList()
      },
    )

  suspend fun openIdFirstLogin(methods: List<AvailableLoginMethod>): OpenIdFirstLogin {
    val ownerCreated =
      runCatchingCancellable(
        attempt = { withContext(contexts.io) { accountApi.ownerCreated() } },
        onFailure = { e ->
          log.w(e) { "Failed to check for a server owner" }
          true
        },
      )
    return when {
      ownerCreated -> NotFirst
      methods.any { it.method == LoginMethod.Password } -> FirstWithPassword
      else -> First
    }
  }

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
              OpenId -> accountApi.login(LoginRequest.OpenId(password, openIdRedirects.returnUrl()))
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

  // An API key, or a session token from OpenID
  suspend fun logIn(token: Token): LoginResult {
    val response =
      runCatchingCancellable(
        attempt = { withContext(contexts.io) { accountApi.validate(token) } },
        onFailure = { e ->
          return e.toLoginResult()
        },
      )

    return when (response) {
      is Failure ->
        if (response.reason == Unauthorized) {
          if (token.isApiKey) InvalidApiKey else TokenExpired
        } else {
          response.reason.toLoginResult()
        }

      is Success -> onValidToken(token)
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
