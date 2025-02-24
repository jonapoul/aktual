package actual.budget.list.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.core.adapted
import actual.api.model.sync.ListUserFilesResponse
import actual.budget.model.Budget
import actual.budget.model.BudgetId
import actual.budget.model.BudgetState
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.Logger
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class BudgetListFetcher @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
  private val logger: Logger,
) {
  suspend fun fetchBudgets(token: LoginToken): FetchBudgetsResult {
    val apis = apisStateHolder.value ?: return FetchBudgetsResult.NotLoggedIn
    return try {
      val response = withContext(contexts.io) {
        apis.sync
          .fetchUserFiles(token)
          .adapted(ActualJson, ListUserFilesResponse.Failure.serializer())
      }
      val result = when (val body = response.body) {
        is ListUserFilesResponse.Failure -> FetchBudgetsResult.FailureResponse(body.reason.reason)
        is ListUserFilesResponse.Success -> FetchBudgetsResult.Success(body.data.map(::toBudget))
      }

      logger.i("Fetched budgets: %s", result)
      result
    } catch (e: CancellationException) {
      throw e
    } catch (e: SerializationException) {
      logger.e(e, "JSON failure fetching budgets")
      FetchBudgetsResult.InvalidResponse(e.requireMessage())
    } catch (e: IOException) {
      logger.e(e, "Network failure fetching budgets")
      FetchBudgetsResult.NetworkFailure(e.requireMessage())
    } catch (e: Exception) {
      logger.e(e, "Failed fetching budgets")
      FetchBudgetsResult.OtherFailure(e.requireMessage())
    }
  }

  private fun toBudget(item: ListUserFilesResponse.Item) = Budget(
    name = item.name,
    state = BudgetState.Unknown,
    encryptKeyId = item.encryptKeyId,
    groupId = item.groupId,
    cloudFileId = BudgetId(item.fileId),
  )
}
