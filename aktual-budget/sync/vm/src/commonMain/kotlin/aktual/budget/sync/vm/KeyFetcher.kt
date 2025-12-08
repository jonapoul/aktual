package aktual.budget.sync.vm

import aktual.api.client.AktualApisStateHolder
import aktual.api.model.sync.GetUserKeyRequest
import aktual.api.model.sync.GetUserKeyResponse
import aktual.budget.encryption.KeyGenerator
import aktual.budget.model.BudgetId
import aktual.core.model.LoginToken
import aktual.core.model.Password
import aktual.prefs.KeyPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import okio.Buffer

@Inject
class KeyFetcher(
  private val stateHolder: AktualApisStateHolder,
  private val contexts: CoroutineContexts,
  private val keyGenerator: KeyGenerator,
  private val keyPreferences: KeyPreferences,
  private val decrypter: Decrypter,
) {
  suspend operator fun invoke(budgetId: BudgetId, token: LoginToken, keyPassword: Password): FetchKeyResult {
    logcat.d { "KeyFetcher $budgetId $token $keyPassword" }
    val syncApi = stateHolder.value?.sync ?: return FetchKeyResult.NotLoggedIn

    val response = try {
      val request = GetUserKeyRequest(budgetId, token)
      withContext(contexts.io) { syncApi.fetchUserKey(request) }
    } catch (e: ResponseException) {
      val failure = e.response.body<GetUserKeyResponse.Failure>()
      logcat.e(e) { "Failure = $failure" }
      return FetchKeyResult.ResponseFailure(failure.reason)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching key for $budgetId and $keyPassword" }
      return FetchKeyResult.IOFailure(e.requireMessage())
    }

    val (keyId, salt, test) = response.data
    val key = keyGenerator(keyPassword, salt)
    keyPreferences.setAndCommit(keyId, key.base64) // committed because we'll use it straight away

    // test the new key
    try {
      val buffer = Buffer()
      buffer.write(test.value.decode())
      val decrypted = decrypter(test.meta, buffer)
      if (decrypted is DecryptResult.Failure) {
        error("Invalid password $keyPassword: $decrypted")
      }

      logcat.i { "Decrypted data successfully with test!" }
      logcat.v { "testBuffer = $buffer" }
      logcat.v { "decrypted = $decrypted" }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      logcat.e(e) { "Failed decrypting buffer from $response with key $key" }
      return FetchKeyResult.TestFailure
    } finally {
      // This is only inserted for the sake of the decryption test
      keyPreferences.delete(keyId)
    }

    return FetchKeyResult.Success(key.base64)
  }
}
