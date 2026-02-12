package aktual.account.domain

import aktual.api.client.AktualApisStateHolder
import aktual.api.model.account.LoginRequest
import aktual.api.model.account.LoginResponse
import aktual.core.model.Password
import aktual.core.prefs.AppGlobalPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

@Inject
class LoginRequester(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: AktualApisStateHolder,
  private val preferences: AppGlobalPreferences,
) {
  suspend fun logIn(password: Password): LoginResult {
    val apis = apisStateHolder.value ?: return LoginResult.OtherFailure("URL not configured")

    // TODO: handle other login methods
    val request = LoginRequest.Password(password)
    val response =
      try {
        withContext(contexts.io) { apis.account.login(request) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: ResponseException) {
        return with(e.response.status) { LoginResult.HttpFailure(value, description) }
      } catch (e: Exception) {
        return when (e) {
          is IOException -> LoginResult.NetworkFailure(e.requireMessage())
          else -> LoginResult.OtherFailure(e.requireMessage())
        }
      }

    val result =
      when (val data = response.data) {
        is LoginResponse.Data.Invalid -> LoginResult.InvalidPassword
        is LoginResponse.Data.Valid -> LoginResult.Success(data.token)
      }

    // Also save the token
    preferences.token.set(response.data.token)

    return result
  }
}
