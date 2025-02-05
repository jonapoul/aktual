package actual.login.vm

import actual.api.client.ActualApisStateHolder
import actual.api.model.account.LoginRequest
import actual.api.model.account.LoginResponse
import actual.core.coroutines.CoroutineContexts
import actual.core.model.Password
import actual.login.prefs.LoginPreferences
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

internal class LoginRequester @Inject constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val loginPreferences: LoginPreferences,
) {
  suspend fun logIn(password: Password): LoginResult {
    val apis = apisStateHolder.value ?: return LoginResult.OtherFailure(reason = "URL not configured")

    val request = LoginRequest(password)
    val response = try {
      withContext(contexts.io) { apis.account.login(request) }
    } catch (e: Exception) {
      Timber.w(e, "Failed logging in with $password")
      return when (e) {
        is HttpException -> LoginResult.HttpFailure(e.code(), e.message())
        is IOException -> LoginResult.NetworkFailure(e.requireMessage())
        else -> LoginResult.OtherFailure(e.requireMessage())
      }
    }

    val result = when (response) {
      is LoginResponse.Error -> LoginResult.OtherFailure(response.reason)
      is LoginResponse.Ok -> when (response.data) {
        is LoginResponse.Data.Invalid -> LoginResult.InvalidPassword
        is LoginResponse.Data.Valid -> LoginResult.Success
      }
    }

    if (response is LoginResponse.Ok) {
      // Cache the token
      loginPreferences.token.set(response.data.token)
    }

    return result
  }
}
