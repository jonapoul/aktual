package aktual.budget.list.vm

import aktual.api.client.SyncApi
import aktual.api.model.sync.ListUserFilesResponse
import aktual.core.model.Token
import aktual.core.runCatchingCancellable
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import java.io.IOException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
class BudgetListFetcher
internal constructor(private val syncApi: SyncApi, private val contexts: CoroutineContexts) {
  suspend fun fetchBudgets(token: Token): FetchBudgetsResult = runCatchingCancellable {
    val response = withContext(contexts.io) { syncApi.fetchUserFiles(token) }
    val result = FetchBudgetsResult.Success(response.data.toImmutableList())
    logcat.d { "Fetched budgets: $result" }
    result
  }
    .getOrElse { e ->
      when (e) {
        is ResponseException -> {
          logcat.e(e) { "HTTP failure fetching budgets" }
          parseResponseException(e)
        }
        is JsonConvertException -> {
          logcat.e(e) { "JSON failure fetching budgets" }
          FetchBudgetsResult.InvalidResponse(e.requireMessage())
        }
        is IOException -> {
          logcat.e(e) { "Network failure fetching budgets" }
          FetchBudgetsResult.NetworkFailure(e.requireMessage())
        }
        else -> {
          logcat.e(e) { "Failed fetching budgets" }
          FetchBudgetsResult.OtherFailure(e.requireMessage())
        }
      }
    }

  private suspend fun parseResponseException(e: ResponseException): FetchBudgetsResult =
    try {
      val body = e.response.body<ListUserFilesResponse.Failure>()
      FetchBudgetsResult.FailureResponse(body.reason)
    } catch (e: JsonConvertException) {
      FetchBudgetsResult.OtherFailure(e.requireMessage())
    }
}
