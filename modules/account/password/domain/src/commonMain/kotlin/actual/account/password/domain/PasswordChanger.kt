package actual.account.password.domain

import actual.account.login.prefs.LoginPreferences
import actual.account.model.Password
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.model.account.ChangePasswordRequest
import actual.api.model.account.ChangePasswordResponse
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.Logger
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class PasswordChanger @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val loginPreferences: LoginPreferences,
  private val logger: Logger,
) {
  suspend fun submit(password: Password): ChangePasswordResult {
    val apis = apisStateHolder.value
    val token = loginPreferences.token.get()
    if (apis == null || token == null) {
      return ChangePasswordResult.NotLoggedIn
    }

    return try {
      val request = ChangePasswordRequest(token, password)
      val response = withContext(contexts.io) { apis.account.changePassword(request) }
      when (response) {
        is ChangePasswordResponse.Error ->
          ChangePasswordResult.OtherFailure(response.reason)

        is ChangePasswordResponse.Ok ->
          ChangePasswordResult.Success
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: HttpException) {
      logger.e(e, "HTTP failure setting password: code=%d, message=%s", e.code(), e.message())
      val response = e.response()
      val failureReason = response
        ?.errorBody()
        ?.string()
        ?.let { ActualJson.decodeFromString(ChangePasswordResponse.Error.serializer(), it) }
        ?.reason
      ChangePasswordResult.OtherFailure(
        reason = failureReason ?: response?.message() ?: e.message(),
      )
    } catch (e: IOException) {
      logger.e(e, "Network failure changing password")
      ChangePasswordResult.NetworkFailure
    } catch (e: Exception) {
      logger.e(e, "Failed changing password")
      ChangePasswordResult.OtherFailure(e.requireMessage())
    }
  }
}
