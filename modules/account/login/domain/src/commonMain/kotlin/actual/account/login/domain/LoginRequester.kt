package actual.account.login.domain

import actual.account.model.Password
import actual.api.client.ActualApisStateHolder
import actual.api.client.adapted
import actual.api.model.account.LoginRequest
import actual.api.model.account.LoginResponse
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class LoginRequester @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val loginPreferences: LoginPreferences,
) {
  suspend fun logIn(password: Password): LoginResult {
    val apis = apisStateHolder.value ?: return LoginResult.OtherFailure("URL not configured")

    val request = LoginRequest(password)
    val response = try {
      withContext(contexts.io) {
        apis.account
          .login(request)
          .adapted(LoginResponse.Failure.serializer())
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      return when (e) {
        is IOException -> LoginResult.NetworkFailure(e.requireMessage())
        else -> LoginResult.OtherFailure(e.requireMessage())
      }
    }

    if (!response.isSuccessful) {
      return LoginResult.HttpFailure(response.code, response.message)
    }

    val body = response.body
    val result = when (body) {
      is LoginResponse.Failure -> LoginResult.OtherFailure(body.reason.reason)
      is LoginResponse.Success -> when (val data = body.data) {
        is LoginResponse.Data.Invalid -> LoginResult.InvalidPassword
        is LoginResponse.Data.Valid -> LoginResult.Success(data.token)
      }
    }

    if (body is LoginResponse.Success) {
      // Cache the token
      loginPreferences.token.set(body.data.token)
    }

    return result
  }
}
