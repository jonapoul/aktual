package actual.account.password.domain

import actual.account.model.Password
import actual.api.client.ActualApisStateHolder
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import actual.prefs.AppLocalPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class PasswordChanger @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val preferences: AppLocalPreferences,
) {
  suspend fun submit(password: Password): ChangePasswordResult {
    val apis = apisStateHolder.value
    val token = preferences.loginToken.get()
    if (apis == null || token == null) {
      return ChangePasswordResult.NotLoggedIn
    }

    return try {
      val request = ChangePasswordRequest(token, password)
      val response = withContext(contexts.io) { apis.account.changePassword(request, token) }
      Logger.v("Received response: $response")
      ChangePasswordResult.Success
    } catch (e: CancellationException) {
      throw e
    } catch (e: ResponseException) {
      val body = e.response.body<ChangePasswordResponse.Failure>()
      Logger.e(e, "HTTP failure changing password: status=${e.response.status}, body=$body")
      ChangePasswordResult.HttpFailure(e.response.status.value, body.reason.reason)
    } catch (e: IOException) {
      Logger.e(e, "Network failure changing password")
      ChangePasswordResult.NetworkFailure
    } catch (e: Exception) {
      Logger.e(e, "Failed changing password")
      ChangePasswordResult.OtherFailure(e.requireMessage())
    }
  }
}
