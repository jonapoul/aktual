package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApisStateHolder
import actual.api.model.sync.GetUserFileInfoResponse
import actual.api.model.sync.UserFile
import actual.budget.model.BudgetId
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import dev.drewhamilton.poko.Poko
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

class BudgetInfoFetcher @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: ActualApisStateHolder,
) {
  sealed interface Result {
    @Poko class Success(val userFile: UserFile) : Result

    sealed interface Failure : Result {
      val reason: String
    }

    data object NotLoggedIn : Failure {
      override val reason = "Not logged in"
    }

    @Poko class IOFailure(override val reason: String) : Failure

    @Poko class HttpFailure(override val reason: String) : Failure
  }

  suspend fun fetch(token: LoginToken, budgetId: BudgetId): Result {
    val api = apisStateHolder.value?.sync
    if (api == null) {
      Logger.w("Not logged in?")
      return Result.NotLoggedIn
    }

    Logger.d("Fetching UserFile for budgetId=%s", budgetId)

    val response = try {
      withContext(contexts.io) { api.fetchUserFileInfo(token, budgetId) }
    } catch (e: ResponseException) {
      Logger.e("Failed fetching UserFile for $budgetId with $token! Response = ${e.response}")
      return e.parseFailure()
    } catch (e: IOException) {
      Logger.e(e, "Failed fetching UserFile of $budgetId with $token")
      return Result.IOFailure(e.requireMessage())
    }

    Logger.v("Fetched UserFile for $budgetId: %s", response.data)
    return Result.Success(response.data)
  }

  private suspend fun ResponseException.parseFailure(): Result {
    val body = try {
      response.body<GetUserFileInfoResponse.Failure>()
    } catch (_: JsonConvertException) {
      return Result.HttpFailure("Received failed HTTP response, but failed parsing body. Status = ${response.status}")
    }
    return Result.HttpFailure(body.reason.reason)
  }
}
