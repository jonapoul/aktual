package aktual.account.domain

import aktual.api.client.AktualApisStateHolder
import aktual.api.model.account.FailureReason
import aktual.api.model.account.LoginRequest
import aktual.api.model.account.LoginResponse
import aktual.core.model.AvailableLoginMethod
import aktual.core.model.LoginMethod
import aktual.core.model.Password
import aktual.core.prefs.AppGlobalPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
class LoginRequester(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: AktualApisStateHolder,
  private val preferences: AppGlobalPreferences,
) {
  suspend fun fetchLoginMethods(): List<AvailableLoginMethod> {
    val accountApi = apisStateHolder.value?.account ?: return emptyList()
    return try {
      withContext(contexts.io) { accountApi.loginMethods().methods }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.w(e) { "Failed to fetch login methods" }
      emptyList()
    }
  }

  suspend fun logIn(
    password: Password,
    loginMethod: LoginMethod = LoginMethod.Password,
  ): LoginResult {
    val accountApi =
      apisStateHolder.value?.account ?: return LoginResult.OtherFailure("URL not configured")

    val response =
      try {
        withContext(contexts.io) {
          when (loginMethod) {
            LoginMethod.Password -> accountApi.login(LoginRequest.Password(password))
            LoginMethod.Header -> accountApi.login(LoginRequest.Header(), password)
            LoginMethod.OpenId -> accountApi.login(LoginRequest.OpenId(password, returnUrl = ""))
          }
        }
      } catch (e: CancellationException) {
        throw e
      } catch (e: IOException) {
        return LoginResult.NetworkFailure(e.requireMessage())
      } catch (e: Exception) {
        return LoginResult.OtherFailure(e.requireMessage())
      }

    return when (response) {
      is LoginResponse.Failure -> response.reason.toLoginResult()
      is LoginResponse.Success -> response.data.toLoginResult()
    }
  }

  private fun FailureReason.toLoginResult(): LoginResult.Failure =
    when (this) {
      FailureReason.TokenExpired -> LoginResult.TokenExpired
      FailureReason.InvalidPassword -> LoginResult.InvalidPassword
      else -> LoginResult.OtherFailure(reason)
    }

  private suspend fun LoginResponse.Data.toLoginResult(): LoginResult =
    when (this) {
      is LoginResponse.Data.Invalid -> {
        LoginResult.InvalidPassword
      }

      is LoginResponse.Data.Redirect -> {
        LoginResult.Redirect(returnUrl)
      }

      is LoginResponse.Data.Valid -> {
        preferences.token.set(token)
        LoginResult.Success(token)
      }
    }
}
