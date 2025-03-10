package actual.account.password.domain

import actual.account.login.domain.LoginPreferences
import actual.account.model.Password
import actual.api.client.ActualApisStateHolder
import actual.api.client.adapted
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class PasswordChanger @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val loginPreferences: LoginPreferences,
) {
  suspend fun submit(password: Password): ChangePasswordResult {
    val apis = apisStateHolder.value
    val token = loginPreferences.token.get()
    if (apis == null || token == null) {
      return ChangePasswordResult.NotLoggedIn
    }

    return try {
      val request = ChangePasswordRequest(token, password)
      val response = withContext(contexts.io) {
        apis.account
          .changePassword(request)
          .adapted(ChangePasswordResponse.Failure.serializer())
      }
      when (val body = response.body) {
        is ChangePasswordResponse.Failure -> ChangePasswordResult.OtherFailure(body.reason.reason)
        is ChangePasswordResponse.Success -> ChangePasswordResult.Success
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: IOException) {
      Logger.e(e, "Network failure changing password")
      ChangePasswordResult.NetworkFailure
    } catch (e: Exception) {
      Logger.e(e, "Failed changing password")
      ChangePasswordResult.OtherFailure(e.requireMessage())
    }
  }
}
