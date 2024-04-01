package dev.jonpoulton.actual.listbudgets.vm

import alakazam.kotlin.core.IODispatcher
import alakazam.kotlin.core.requireMessage
import dev.jonpoulton.actual.api.client.ActualApisStateHolder
import dev.jonpoulton.actual.api.model.sync.ListFilesResponse
import dev.jonpoulton.actual.login.prefs.LoginPreferences
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

internal class BudgetsFetcher @Inject constructor(
  private val io: IODispatcher,
  private val apisStateHolder: ActualApisStateHolder,
  private val loginPreferences: LoginPreferences,
) {
  suspend fun fetch(): FetchBudgetsResult {
    val apis = apisStateHolder.peek() ?: return FetchBudgetsResult.OtherFailure(reason = "URL not configured")
    val token = loginPreferences.token.get() ?: return FetchBudgetsResult.OtherFailure(reason = "Not logged in")

    val response = try {
      withContext(io) { apis.sync.listFiles(token) }
    } catch (e: Exception) {
      Timber.w(e, "Failed fetching budgets with $token")
      return when (e) {
        is HttpException -> FetchBudgetsResult.HttpFailure(e.code(), e.message())
        is IOException -> FetchBudgetsResult.NetworkFailure(e.requireMessage())
        else -> FetchBudgetsResult.OtherFailure(e.requireMessage())
      }
    }

    val budgetFiles = response.data
      .map { data -> data.toBudgetFile() }
      .toImmutableList()
    return FetchBudgetsResult.Success(budgetFiles)
  }

  private fun ListFilesResponse.Data.toBudgetFile(): BudgetFile = when {

  }
}
