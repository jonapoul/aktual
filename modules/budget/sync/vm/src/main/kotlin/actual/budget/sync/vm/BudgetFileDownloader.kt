package actual.budget.sync.vm

import actual.account.model.LoginToken
import actual.api.client.ActualApis
import actual.api.client.ActualApisStateHolder
import actual.budget.model.BudgetId
import actual.budget.sync.vm.Bytes.Companion.Zero
import actual.budget.sync.vm.DownloadState.Done
import actual.budget.sync.vm.DownloadState.Failed
import actual.budget.sync.vm.DownloadState.InProgress
import actual.log.Logger
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
import okio.buffer
import okio.sink
import okio.source
import okio.use
import java.io.File
import java.io.IOException
import javax.inject.Inject

class BudgetFileDownloader @Inject internal constructor(
  private val contexts: CoroutineContexts,
  private val fileStore: FileStore,
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

    val body = response.body()
    if (response.isSuccessful && body != null) {
      val budgetFile = fileStore.budgetFile(id)
      saveFile(body, budgetFile)
    } else {
      Logger.e("Failed: $response")
      emit(Failed(response.message(), Zero))
    }
  }

  @Suppress("NestedBlockDepth", "BlockingMethodInNonBlockingContext")
  private suspend fun FlowCollector<DownloadState>.saveFile(body: ResponseBody, outputFile: File) {
    if (outputFile.exists()) {
      outputFile.delete()
      outputFile.createNewFile()
    }

    val responseLength = body.contentLength().bytes
    emit(InProgress(Zero, responseLength))

    try {
      body.byteStream().source().buffer().use { source ->
        outputFile.sink(append = false).buffer().use { sink ->
          val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
          var readSoFar = 0L
          var bytes = source.read(buffer)
          while (bytes >= 0) {
            sink.write(buffer, 0, bytes)
            readSoFar += bytes
            bytes = source.read(buffer)
            emit(InProgress(readSoFar.bytes, responseLength))
          }
        }
      }
      emit(Done(outputFile, responseLength))
      Logger.i("Successfully downloaded $responseLength to $outputFile")
    } catch (e: IOException) {
      Logger.e(e, "Failed downloading $responseLength to $outputFile")
      emit(Failed(e.requireMessage(), responseLength))
    }
  }
}
