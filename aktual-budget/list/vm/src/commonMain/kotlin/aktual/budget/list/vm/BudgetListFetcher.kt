package aktual.budget.list.vm

import aktual.api.client.SyncApi
import aktual.api.model.sync.ListUserFilesResponse
import aktual.core.model.Token
import alakazam.kotlin.CoroutineContexts
import alakazam.kotlin.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import java.io.IOException
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat

@Inject
class BudgetListFetcher(private val syncApi: SyncApi, private val contexts: CoroutineContexts) {
  suspend fun fetchBudgets(token: Token): FetchBudgetsResult =
    try {
      val response = withContext(contexts.io) { syncApi.fetchUserFiles(token) }
      val result = FetchBudgetsResult.Success(response.data.toImmutableList())
      logcat.d { "Fetched budgets: $result" }
      result
    } catch (e: CancellationException) {
      throw e
    } catch (e: ResponseException) {
      logcat.e(e) { "HTTP failure fetching budgets" }
      parseResponseException(e)
    } catch (e: JsonConvertException) {
      logcat.e(e) { "JSON failure fetching budgets" }
      FetchBudgetsResult.InvalidResponse(e.requireMessage())
    } catch (e: IOException) {
      logcat.e(e) { "Network failure fetching budgets" }
      FetchBudgetsResult.NetworkFailure(e.requireMessage())
    } catch (e: Exception) {
      logcat.e(e) { "Failed fetching budgets" }
      FetchBudgetsResult.OtherFailure(e.requireMessage())
    }

  private suspend fun parseResponseException(e: ResponseException): FetchBudgetsResult =
    try {
      val body = e.response.body<ListUserFilesResponse.Failure>()
      FetchBudgetsResult.FailureResponse(body.reason)
    } catch (e: JsonConvertException) {
      FetchBudgetsResult.OtherFailure(e.requireMessage())
    }
}
