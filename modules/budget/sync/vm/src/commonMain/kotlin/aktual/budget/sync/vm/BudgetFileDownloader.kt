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
package aktual.budget.sync.vm

import aktual.api.client.AktualApisStateHolder
import aktual.api.client.SyncDownloadApi
import aktual.api.client.SyncDownloadState
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.encryptedZip
import aktual.budget.sync.vm.DownloadState.Done
import aktual.budget.sync.vm.DownloadState.Failure
import aktual.budget.sync.vm.DownloadState.InProgress
import aktual.core.model.Bytes.Companion.Zero
import aktual.core.model.LoginToken
import aktual.core.model.bytes
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import logcat.logcat
import java.io.IOException

@Inject
class BudgetFileDownloader internal constructor(
  private val contexts: CoroutineContexts,
  private val budgetFiles: BudgetFiles,
  private val apisStateHolder: AktualApisStateHolder,
) {
  fun download(token: LoginToken, budgetId: BudgetId): Flow<DownloadState> {
    val api = apisStateHolder.value?.syncDownload ?: return flowOf(Failure.NotLoggedIn)
    return flow { emitState(api, token, budgetId) }.flowOn(contexts.io)
  }

  private suspend fun FlowCollector<DownloadState>.emitState(api: SyncDownloadApi, token: LoginToken, id: BudgetId) {
    val destinationPath = budgetFiles.encryptedZip(id, mkdirs = true)
    try {
      emit(InProgress(Zero, Zero))
      api.downloadUserFile(token, id, destinationPath).collect { state ->
        when (state) {
          is SyncDownloadState.InProgress -> emit(InProgress(state))
          is SyncDownloadState.Done -> emit(Done(state))
        }
      }
    } catch (e: ResponseException) {
      logcat.e(e) { "HTTP failure downloading $id with $token" }
      deleteDir(id)
      with(e.response.status) { emit(Failure.Http(value, description, Zero)) }
      return
    } catch (e: IOException) {
      logcat.e(e) { "Failed fetching initial download request for $id with $token" }
      deleteDir(id)
      emit(Failure.IO(e.requireMessage(), Zero))
      return
    }
  }

  private fun InProgress(state: SyncDownloadState.InProgress) = InProgress(
    read = state.bytesSentTotal.bytes,
    total = state.contentLength.bytes,
  )

  private fun Done(state: SyncDownloadState.Done) = Done(
    path = state.path,
    total = state.contentLength.bytes,
  )

  private fun deleteDir(id: BudgetId) {
    with(budgetFiles) {
      val dir = directory(id)
      fileSystem.deleteRecursively(dir)
    }
  }
}
