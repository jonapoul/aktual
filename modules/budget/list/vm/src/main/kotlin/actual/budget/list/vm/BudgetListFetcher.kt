package actual.budget.list.vm

import actual.api.client.ActualApisStateHolder
import actual.api.client.ActualJson
import actual.api.model.sync.ListUserFilesResponse
import actual.core.coroutines.CoroutineContexts
import actual.log.Logger
import actual.login.model.LoginToken
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
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
      val response = withContext(contexts.io) { apis.sync.listUserFiles(token) }
      when (response) {
        is ListUserFilesResponse.Error ->
          FetchBudgetsResult.OtherFailure(response.reason)

        is ListUserFilesResponse.Ok ->
          FetchBudgetsResult.Success(
            budgets = response.data.map { item ->
              Budget(
                name = item.name,
                state = BudgetState.Unknown,
                encryptKeyId = item.encryptKeyId,
                groupId = item.groupId,
                cloudFileId = item.fileId,
              )
            },
          )
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: HttpException) {
      logger.e(e, "HTTP failure fetching budgets")
      val response = e.response()
      val failureReason = response
        ?.errorBody()
        ?.string()
        ?.let { ActualJson.decodeFromString(ListUserFilesResponse.Error.serializer(), it) }
        ?.reason
      FetchBudgetsResult.FailureResponse(reason = failureReason ?: response?.message())
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
}
