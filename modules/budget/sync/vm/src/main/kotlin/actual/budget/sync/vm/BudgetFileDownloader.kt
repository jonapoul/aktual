package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApisStateHolder
import actual.api.client.SyncApi
import actual.budget.model.BudgetId
import actual.budget.sync.vm.Bytes.Companion.Zero
import actual.budget.sync.vm.DownloadState.Done
import actual.budget.sync.vm.DownloadState.Failure
import actual.budget.sync.vm.DownloadState.InProgress
import actual.core.files.DatabaseDirectory
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import okio.buffer
import java.io.IOException
import javax.inject.Inject

class BudgetFileDownloader @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val databaseDirectory: DatabaseDirectory,
  private val apisStateHolder: ActualApisStateHolder,
) {
  fun download(token: LoginToken, budgetId: BudgetId): Flow<DownloadState> {
    val api = apisStateHolder.value?.sync ?: return flowOf(Failure.NotLoggedIn)
    return flow { emitState(api, token, budgetId) }.flowOn(contexts.io)
  }

  private suspend fun FlowCollector<DownloadState>.emitState(syncApi: SyncApi, token: LoginToken, id: BudgetId) {
    val response = try {
      emit(InProgress(Zero, Zero))
      syncApi.downloadUserFile(token, id)
    } catch (e: ResponseException) {
      Logger.e(e, "HTTP failure downloading $id with $token")
      with(e.response.status) { emit(Failure.Http(value, description, Zero)) }
      return
    } catch (e: IOException) {
      Logger.e(e, "Failed downloading $id with $token")
      emit(Failure.IO(e.requireMessage(), Zero))
      return
    }

    val length = response.contentLength()?.bytes
    if (length != null) {
      Logger.i("Starting download for $id with length $length")
      saveFileAndEmitProgress(response, length, id)
    } else {
      Logger.e("Unknown content length: $response")
      emit(Failure.Other(response.status.description, Zero))
    }
  }

  @Suppress("NestedBlockDepth")
  private suspend fun FlowCollector<DownloadState>.saveFileAndEmitProgress(
    response: HttpResponse,
    responseLength: Bytes,
    budgetId: BudgetId,
  ) {
    var totalBytesRead = 0L
    try {
      databaseDirectory.sink(budgetId).buffer().use { sink ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val input = response.bodyAsChannel()
        emit(InProgress(Zero, responseLength))
        while (!input.isClosedForRead) {
          val bytesRead = input.readAvailable(buffer)
          if (bytesRead <= 0) break
          sink.write(buffer, 0, bytesRead)
          totalBytesRead += bytesRead
          emit(InProgress(totalBytesRead.bytes, responseLength))
        }
      }
      emit(Done(responseLength, path = databaseDirectory.pathFor(budgetId)))
      Logger.i("Successfully downloaded $responseLength for ID $budgetId")
    } catch (e: IOException) {
      Logger.e(e, "Failed downloading $responseLength for ID $budgetId")
      emit(Failure.InProgress(e.requireMessage(), totalBytesRead.bytes, responseLength))
    }
  }
}
