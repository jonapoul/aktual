package aktual.budget.sync.vm

import aktual.api.client.SyncApi
import aktual.api.model.sync.GetUserKeyRequest
import aktual.api.model.sync.GetUserKeyResponse
import aktual.budget.encryption.BufferDecrypter
import aktual.budget.encryption.generateKeyFromPassword
import aktual.budget.model.BudgetId
import aktual.core.model.Password
import aktual.core.model.Token
import aktual.core.utils.runCatchingCancellable
import aktual.log
import aktual.prefs.KeyPreferences
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import okio.Buffer

@Inject
class KeyFetcher
internal constructor(
  private val token: Token,
  private val syncApi: SyncApi,
  private val contexts: CoroutineContexts,
  private val keyPreferences: KeyPreferences,
  private val decrypter: BufferDecrypter,
) {
  suspend operator fun invoke(budgetId: BudgetId, keyPassword: Password): FetchKeyResult {
    log.d { "KeyFetcher $budgetId $token $keyPassword" }
    val response =
      runCatchingCancellable(
        attempt = {
          val request = GetUserKeyRequest(budgetId, token)
          withContext(contexts.io) { syncApi.fetchUserKey(request) }
        },
        onFailure = { e ->
          if (e is ResponseException) {
            val failure = e.response.body<GetUserKeyResponse.Failure>()
            log.e(e) { "Failure = $failure" }
            return FetchKeyResult.ResponseFailure(failure.reason)
          }
          log.e(e) { "Failed fetching key for $budgetId and $keyPassword" }
          return FetchKeyResult.IOFailure(e.requireMessage())
        },
      )

    val (keyId, salt, test) = response.data
    val key = generateKeyFromPassword(keyPassword, salt)
    keyPreferences[keyId] = key.encoded

    try {
      val buffer = Buffer()
      buffer.write(test.value)
      val decrypted = decrypter(test.meta, buffer)
      if (decrypted is Failure) {
        error("Invalid password ${keyPassword.value}: $decrypted")
      }

      log.i { "Decrypted data successfully with test!" }
      log.v { "testBuffer = $buffer" }
      log.v { "decrypted = $decrypted" }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      log.e(e) { "Failed decrypting buffer from $response with key $key" }
      return TestFailure
    } finally {
      // This is only inserted for the sake of the decryption test
      withContext(NonCancellable) { keyPreferences.delete(keyId) }
    }

    return FetchKeyResult.Success(key.encoded)
  }
}
