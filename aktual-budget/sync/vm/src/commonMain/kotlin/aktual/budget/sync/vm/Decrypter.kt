package aktual.budget.sync.vm

import aktual.api.model.sync.EncryptMeta
import aktual.budget.encryption.UnknownAlgorithmException
import aktual.budget.encryption.decryptToSink
import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.decryptedZip
import aktual.prefs.KeyPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import okio.Buffer
import okio.Path
import okio.Sink
import okio.Source

// Adapted from packages/loot-core/src/server/encryption/encryption-internals.ts
@Inject
class Decrypter(
  private val contexts: CoroutineContexts,
  private val keys: KeyPreferences,
  private val files: BudgetFiles,
) {
  /**
   * Decrypts a file at the given [filePath] and creates another at [BudgetFiles.decryptedZip].
   */
  suspend operator fun invoke(id: BudgetId, meta: EncryptMeta, filePath: Path): DecryptResult {
    val decryptedPath = files.decryptedZip(id, mkdirs = true)
    logcat.d { "Decrypting $id from $filePath into $decryptedPath with $meta" }
    return decrypt(
      meta = meta,
      source = files.fileSystem.source(filePath),
      sink = files.fileSystem.sink(decryptedPath),
      result = { DecryptResult.DecryptedFile(decryptedPath) },
    )
  }

  /**
   * Decrypts a [Buffer] of bytes and returns another containing the decrypted data
   */
  suspend operator fun invoke(meta: EncryptMeta, buffer: Buffer): DecryptResult {
    logcat.d { "Decrypting ${buffer.size} bytes with $meta" }
    val output = Buffer()
    return decrypt(
      meta = meta,
      source = buffer,
      sink = output,
      result = { DecryptResult.DecryptedBuffer(output) },
    )
  }

  private suspend fun decrypt(
    meta: EncryptMeta,
    source: Source,
    sink: Sink,
    result: () -> DecryptResult.Success,
  ): DecryptResult = try {
    val key = keys[meta.keyId] ?: return DecryptResult.MissingKey
    withContext(contexts.io) {
      source.decryptToSink(
        key = key.decode(),
        iv = meta.iv.decode(),
        authTag = meta.authTag.decode(),
        algorithm = meta.algorithm,
        sink = sink,
      )
    }
    result()
  } catch (e: UnknownAlgorithmException) {
    DecryptResult.UnknownAlgorithm(e.algorithm)
  } catch (e: CancellationException) {
    throw e
  } catch (e: Exception) {
    DecryptResult.OtherFailure(e.requireMessage())
  } finally {
    runCatching { source.close() }
    runCatching { sink.close() }
  }
}
