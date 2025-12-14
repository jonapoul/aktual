package aktual.budget.encryption

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.encryptedZip
import aktual.core.model.KeyId
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.toByteString
import okio.CipherSink
import okio.Path
import okio.Sink
import okio.Source
import okio.buffer
import okio.use
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

// Adapted from packages/loot-core/src/server/encryption/encryption-internals.ts

/**
 * Encrypts a file at the given [filePath] and creates another at the encrypted path.
 */
fun interface FileEncrypter {
  suspend operator fun invoke(id: BudgetId, keyId: KeyId, filePath: Path): EncryptResult
}

/**
 * Encrypts a [Buffer] of bytes and returns another containing the encrypted data
 */
fun interface BufferEncrypter {
  suspend operator fun invoke(keyId: KeyId, buffer: Buffer): EncryptResult
}

sealed interface EncryptResult {
  sealed interface Success : EncryptResult {
    val meta: Meta
  }

  data class EncryptedBuffer(val buffer: Buffer, override val meta: Meta) : Success
  data class EncryptedFile(val path: Path, override val meta: Meta) : Success

  sealed interface Failure : EncryptResult
  data object MissingKey : Failure
  data class UnknownAlgorithm(val algorithm: String) : Failure
  data class OtherFailure(val message: String) : Failure
}

@BindingContainer
@ContributesTo(AppScope::class)
internal interface EncrypterContainer {
  @Binds val FileEncrypterImpl.binds: FileEncrypter
  @Binds val BufferEncrypterImpl.binds: BufferEncrypter
}

@Inject
@ContributesBinding(AppScope::class)
internal class FileEncrypterImpl(
  private val contexts: CoroutineContexts,
  private val keys: EncryptionKeys,
  private val random: Random,
  private val files: BudgetFiles,
) : FileEncrypter {
  override suspend fun invoke(id: BudgetId, keyId: KeyId, filePath: Path): EncryptResult {
    val encryptedPath = files.encryptedZip(id, mkdirs = true)
    logcat.d { "Encrypting $id from $filePath into $encryptedPath with keyId=$keyId" }
    return encrypt(
      contexts = contexts,
      keys = keys,
      keyId = keyId,
      random = random,
      source = files.fileSystem.source(filePath),
      sink = files.fileSystem.sink(encryptedPath),
      result = { meta -> EncryptResult.EncryptedFile(encryptedPath, meta) },
    )
  }
}

@Inject
@ContributesBinding(AppScope::class)
internal class BufferEncrypterImpl(
  private val contexts: CoroutineContexts,
  private val keys: EncryptionKeys,
  private val random: Random,
) : BufferEncrypter {
  override suspend operator fun invoke(keyId: KeyId, buffer: Buffer): EncryptResult {
    logcat.d { "Encrypting ${buffer.size} bytes with keyId=$keyId" }
    val output = Buffer()
    return encrypt(
      contexts = contexts,
      keys = keys,
      keyId = keyId,
      random = random,
      source = buffer,
      sink = output,
      result = { meta -> EncryptResult.EncryptedBuffer(output, meta) },
    )
  }
}

private suspend fun encrypt(
  keys: EncryptionKeys,
  contexts: CoroutineContexts,
  keyId: KeyId,
  random: Random,
  source: Source,
  sink: Sink,
  result: (Meta) -> EncryptResult.Success,
): EncryptResult = try {
  val key = keys[keyId] ?: return EncryptResult.MissingKey
  val meta = withContext(contexts.io) {
    encryptToSink(
      key = key,
      keyId = keyId,
      random = random,
      source = source,
      sink = sink,
    )
  }
  result(meta)
} catch (e: UnknownAlgorithmException) {
  EncryptResult.UnknownAlgorithm(e.algorithm)
} catch (e: CancellationException) {
  throw e
} catch (e: Exception) {
  EncryptResult.OtherFailure(e.requireMessage())
} finally {
  if (sink !is Buffer) runCatching { sink.close() }
  if (source !is Buffer) runCatching { source.close() }
}

@Throws(UnknownAlgorithmException::class)
internal fun encryptToSink(
  key: ByteString,
  keyId: KeyId,
  source: Source,
  sink: Sink,
  random: Random,
): Meta {
  // Generate random IV
  val iv = random.nextBytes(GCM_IV_LENGTH)

  val cipher = Cipher.getInstance(AES_GCM_CIPHER_TRANSFORMATION)
  val keySpec = SecretKeySpec(key.toByteArray(), CIPHER_ALGORITHM)
  val gcmSpec = GCMParameterSpec(AUTH_TAG_LENGTH, iv)
  cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)

  // Encrypt to a temporary buffer to extract the auth tag
  // In GCM mode, the cipher automatically appends the auth tag to the ciphertext
  val encryptedWithTag = Buffer()
  CipherSink(encryptedWithTag.buffer(), cipher).buffer().use { cipherSink ->
    source.buffer().use { src ->
      cipherSink.writeAll(src)
    }
  }

  // In GCM mode, the last AUTH_TAG_LENGTH bits are the authentication tag
  @Suppress("MagicNumber")
  val authTagLengthBytes = AUTH_TAG_LENGTH / 8
  val totalSize = encryptedWithTag.size
  val ciphertextSize = totalSize - authTagLengthBytes

  // Extract the ciphertext (everything except the last 16 bytes)
  val ciphertext = Buffer()
  ciphertext.write(encryptedWithTag, ciphertextSize)

  // Extract the auth tag (last 16 bytes)
  val authTag = ByteArray(authTagLengthBytes)
  encryptedWithTag.read(authTag)

  // Write the ciphertext (without auth tag) to the sink
  sink.buffer().use { s ->
    s.writeAll(ciphertext)
  }

  return DefaultMeta(
    keyId = keyId,
    algorithm = EXPECTED_ALGORITHM,
    iv = iv.toByteString(),
    authTag = authTag.toByteString(),
  )
}

private const val GCM_IV_LENGTH = 12 // 96 bits is recommended for GCM
