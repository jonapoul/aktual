package aktual.account.vm

import aktual.api.client.AccountApi
import aktual.api.model.account.ChangePasswordRequest
import aktual.api.model.account.ChangePasswordResponse
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.core.runCatchingCancellable
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import java.io.IOException
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
internal class PasswordChanger(
  private val token: Token,
  private val accountApi: AccountApi,
  private val contexts: CoroutineContexts,
) {
  suspend fun submit(password: Password): ChangePasswordResult =
    runCatchingCancellable<ChangePasswordResult>(
      attempt = {
        val request = ChangePasswordRequest(password)
        val response = withContext(contexts.io) { accountApi.changePassword(request, token) }
        logcat.v { "Received response: $response" }
        Success
      },
      onFailure = { e ->
        when (e) {
          is ResponseException -> {
            val body = e.response.body<ChangePasswordResponse.Failure>()
            logcat.e(e) {
              "HTTP failure changing password: status=${e.response.status}, body=$body"
            }
            ChangePasswordResult.HttpFailure(e.response.status.value, body.reason.reason)
          }
          is IOException -> {
            logcat.e(e) { "Network failure changing password" }
            NetworkFailure
          }
          else -> {
            logcat.e(e) { "Failed changing password" }
            ChangePasswordResult.OtherFailure(e.requireMessage())
          }
        }
      },
    )
}
