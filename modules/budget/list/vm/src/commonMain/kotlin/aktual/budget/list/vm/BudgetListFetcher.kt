/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package aktual.budget.list.vm

import aktual.api.client.AktualApisStateHolder
import aktual.api.model.sync.ListUserFilesResponse
import aktual.api.model.sync.UserFile
import aktual.budget.model.Budget
import aktual.budget.model.BudgetState
import aktual.core.model.LoginToken
import aktual.prefs.KeyPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.JsonConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import java.io.IOException

@Inject
class BudgetListFetcher internal constructor(
  private val contexts: CoroutineContexts,
  private val apisStateHolder: AktualApisStateHolder,
  private val keyPreferences: KeyPreferences,
) {
  suspend fun fetchBudgets(token: LoginToken): FetchBudgetsResult {
    val apis = apisStateHolder.value ?: return FetchBudgetsResult.NotLoggedIn
    return try {
      val response = withContext(contexts.io) { apis.sync.fetchUserFiles(token) }
      val result = FetchBudgetsResult.Success(response.data.map(::toBudget))
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
