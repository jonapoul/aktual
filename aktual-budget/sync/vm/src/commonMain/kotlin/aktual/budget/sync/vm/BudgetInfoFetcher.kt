package aktual.budget.sync.vm

import aktual.api.client.SyncApi
import aktual.api.model.account.FailureReason
import aktual.api.model.sync.GetUserFileInfoResponse
import aktual.api.model.sync.UserFile
import aktual.budget.model.BudgetId
import aktual.core.model.Token
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import java.io.IOException
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
class BudgetInfoFetcher(
  private val token: Token,
  private val syncApi: SyncApi,
  private val contexts: CoroutineContexts,
) {
  sealed interface Result {
    data class Success(val userFile: UserFile) : Result

    sealed interface Failure : Result {
      val reason: String
    }

    data class IOFailure(override val reason: String) : Failure

    data class HttpFailure(val failureReason: FailureReason) : Failure {
      override val reason: String
        get() = failureReason.reason
    }
  }

  suspend fun fetch(budgetId: BudgetId): Result {
    logcat.d { "Fetching UserFile for budgetId=$budgetId" }

    val response =
      try {
        withContext(contexts.io) { syncApi.fetchUserFileInfo(token, budgetId) }
      } catch (e: ResponseException) {
        logcat.e { "Failed fetching UserFile for $budgetId with $token! Response = ${e.response}" }
        return e.parseFailure()
      } catch (e: IOException) {
        logcat.e(e) { "Failed fetching UserFile of $budgetId with $token" }
        return Result.IOFailure(e.requireMessage())
      }

    logcat.v { "Fetched UserFile for $budgetId: ${response.data}" }
    return Result.Success(response.data)
  }

  private suspend fun ResponseException.parseFailure(): Result {
    val body =
      try {
        response.body<GetUserFileInfoResponse.Failure>()
      } catch (_: JsonConvertException) {
        return Result.HttpFailure(FailureReason("http-${response.status.value}"))
      }
    return Result.HttpFailure(body.reason)
  }
}
