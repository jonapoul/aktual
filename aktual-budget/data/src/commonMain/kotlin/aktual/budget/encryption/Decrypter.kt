package aktual.budget.encryption

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.decryptedZip
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import okio.Buffer
import okio.ByteString
import okio.CipherSource
import okio.IOException
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer
import okio.use
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

// Adapted from packages/loot-core/src/server/encryption/encryption-internals.ts

/**
 * Decrypts a file at the given [filePath] and creates another at [decryptedZip].
 */
fun interface FileDecrypter {
  suspend operator fun invoke(id: BudgetId, meta: Meta, filePath: Path): DecryptResult
}

/**
 * Decrypts a [Buffer] of bytes and returns another containing the decrypted data
 */
fun interface BufferDecrypter {
  suspend operator fun invoke(meta: Meta, buffer: Buffer): DecryptResult
}

sealed interface DecryptResult {
  sealed interface Success : DecryptResult
  data class DecryptedBuffer(val buffer: Buffer) : Success
  data class DecryptedFile(val path: Path) : Success
  data class NotNeeded(val path: Path) : Success

  sealed interface Failure : DecryptResult
  data object FailedFetchingKey : Failure
  data object MissingKey : Failure
  data class UnknownAlgorithm(val algorithm: String) : Failure
  data class OtherFailure(val message: String) : Failure
}

@Inject
@ContributesBinding(AppScope::class)
class FileDecrypterImpl(
  private val contexts: CoroutineContexts,
  private val keys: EncryptionKeys,
  private val files: BudgetFiles,
) : FileDecrypter {
  override suspend fun invoke(id: BudgetId, meta: Meta, filePath: Path): DecryptResult {
    val decryptedPath = files.decryptedZip(id, mkdirs = true)
    logcat.d { "Decrypting $id from $filePath into $decryptedPath with $meta" }
    return decrypt(
      contexts = contexts,
      keys = keys,
      meta = meta,
      source = files.fileSystem.source(filePath),
      sink = files.fileSystem.sink(decryptedPath),
      result = { DecryptResult.DecryptedFile(decryptedPath) },
    )
  }
}

@Inject
@ContributesBinding(AppScope::class)
class BufferDecrypterImpl(
  private val contexts: CoroutineContexts,
  private val keys: EncryptionKeys,
) : BufferDecrypter {
  override suspend operator fun invoke(meta: Meta, buffer: Buffer): DecryptResult {
    logcat.d { "Decrypting ${buffer.size} bytes with $meta" }
    val output = Buffer()
    return decrypt(
      contexts = contexts,
      keys = keys,
      meta = meta,
      source = buffer,
      sink = output,
      result = { DecryptResult.DecryptedBuffer(output) },
    )
  }
}

private suspend fun decrypt(
  keys: EncryptionKeys,
  contexts: CoroutineContexts,
  meta: Meta,
  source: Source,
  sink: Sink,
  result: () -> DecryptResult.Success,
): DecryptResult = try {
  val key = keys[meta.keyId] ?: return DecryptResult.MissingKey
  withContext(contexts.io) {
    decryptToSink(
      key = key.toByteArray(),
      iv = meta.iv.toByteArray(),
      authTag = meta.authTag.toByteArray(),
      algorithm = meta.algorithm,
      source = source,
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
  if (sink !is Buffer) runCatching { sink.close() }
  if (source !is Buffer) runCatching { source.close() }
}

@Throws(UnknownAlgorithmException::class)
internal fun decryptToSink(
  key: ByteArray,
  iv: ByteArray,
  authTag: ByteArray,
  algorithm: String,
  source: Source,
  sink: Sink,
) {
  val cipherTransformation = when (algorithm.lowercase()) {
    EXPECTED_ALGORITHM -> AES_GCM_CIPHER_TRANSFORMATION
    else -> throw UnknownAlgorithmException(algorithm)
  }

  val cipher = Cipher.getInstance(cipherTransformation)
  val keySpec = SecretKeySpec(key, CIPHER_ALGORITHM)
  val gcmSpec = GCMParameterSpec(AUTH_TAG_LENGTH, iv)
  cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)

  // We need to append the auth tag to our encrypted source
  val taggedSource = source + Buffer().apply { write(authTag) }

  CipherSource(taggedSource.buffer(), cipher).buffer().use { src ->
    sink.buffer().use { s ->
      s.writeAll(src)
    }
  }
}

internal const val EXPECTED_ALGORITHM = "aes-256-gcm"
internal const val AES_GCM_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
internal const val AUTH_TAG_LENGTH = 128
internal const val CIPHER_ALGORITHM = "AES"

class UnknownAlgorithmException(val algorithm: String) : IOException()
