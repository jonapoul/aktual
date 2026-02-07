package aktual.budget.encryption

import aktual.budget.model.BudgetId
import aktual.core.model.KeyId
import okio.Buffer
import okio.Path

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
