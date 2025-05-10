package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.budget.model.BudgetId
import actual.budget.sync.vm.Bytes.Companion.Zero
import actual.budget.sync.vm.DownloadState.Done
import actual.budget.sync.vm.DownloadState.Failed
import actual.budget.sync.vm.DownloadState.InProgress
import actual.core.files.DatabaseDirectory
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
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
    val apis = apisStateHolder.value ?: error("URL not configured")
    return flow { emitState(apis, token, budgetId) }
      .flowOn(contexts.io)
  }

  private suspend fun FlowCollector<DownloadState>.emitState(apis: ActualApis, token: LoginToken, id: BudgetId) {
    val response = try {
      apis.sync.downloadUserFile(token, id)
    } catch (e: IOException) {
      Logger.e(e, "Failed downloading $id with $token")
      emit(Failed(e.requireMessage(), Zero))
      return
    }

    val length = response.contentLength()?.bytes
    when {
      length == null -> {
        Logger.e("Empty response: $response")
        emit(Failed(response.status.description, Zero))
      }

      !response.status.isSuccess() -> {
        Logger.e("Failed: ${response.status}")
        emit(Failed(response.status.description, Zero))
      }

      else -> {
        Logger.i("Starting download for $id with length $length")
        saveFileAndEmitProgress(response, length, id)
      }
    }
  }

  @Suppress("NestedBlockDepth")
  private suspend fun FlowCollector<DownloadState>.saveFileAndEmitProgress(
    response: HttpResponse,
    responseLength: Bytes,
    budgetId: BudgetId,
  ) {
    try {
      databaseDirectory.sink(budgetId).buffer().use { sink ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var totalBytesRead = 0L
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
      emit(Done(responseLength))
      Logger.i("Successfully downloaded $responseLength for ID $budgetId")
    } catch (e: IOException) {
      Logger.e(e, "Failed downloading $responseLength for ID $budgetId")
      emit(Failed(e.requireMessage(), responseLength))
    }
  }
}
