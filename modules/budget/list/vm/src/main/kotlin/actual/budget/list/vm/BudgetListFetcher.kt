package actual.budget.list.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApisStateHolder
import actual.api.model.sync.ListUserFilesResponse
import actual.api.model.sync.UserFile
import actual.budget.model.Budget
import actual.budget.model.BudgetState
import actual.prefs.KeyPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class BudgetListFetcher @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val keyPreferences: KeyPreferences,
) {
  suspend fun fetchBudgets(token: LoginToken): FetchBudgetsResult {
    val apis = apisStateHolder.value ?: return FetchBudgetsResult.NotLoggedIn
    return try {
      val response = withContext(contexts.io) { apis.sync.fetchUserFiles(token) }
      val result = FetchBudgetsResult.Success(response.data.map(::toBudget))
      Logger.i("Fetched budgets: %s", result)
      result
    } catch (e: CancellationException) {
      throw e
    } catch (e: ResponseException) {
      Logger.e(e, "HTTP failure fetching budgets")
      parseResponseException(e)
    } catch (e: JsonConvertException) {
      Logger.e(e, "JSON failure fetching budgets")
      FetchBudgetsResult.InvalidResponse(e.requireMessage())
    } catch (e: IOException) {
      Logger.e(e, "Network failure fetching budgets")
      FetchBudgetsResult.NetworkFailure(e.requireMessage())
    } catch (e: Exception) {
      Logger.e(e, "Failed fetching budgets")
      FetchBudgetsResult.OtherFailure(e.requireMessage())
    }
  }

  private suspend fun parseResponseException(e: ResponseException): FetchBudgetsResult = try {
    val body = e.response.body<ListUserFilesResponse.Failure>()
    FetchBudgetsResult.FailureResponse(body.reason.reason)
  } catch (e: JsonConvertException) {
    FetchBudgetsResult.OtherFailure(e.requireMessage())
  }

  private fun toBudget(item: UserFile) = Budget(
    name = item.name,
    state = BudgetState.Unknown,
    encryptKeyId = item.encryptKeyId?.value,
    groupId = item.groupId,
    cloudFileId = item.fileId,
    hasKey = item.encryptKeyId in keyPreferences,
  )
}
