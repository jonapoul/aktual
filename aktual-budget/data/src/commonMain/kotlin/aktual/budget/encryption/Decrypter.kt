package aktual.budget.encryption

import aktual.budget.model.BudgetId
import aktual.budget.model.decryptedZip
import okio.Buffer
import okio.Path

// Adapted from packages/loot-core/src/server/encryption/encryption-internals.ts

/** Decrypts a file at the given [filePath] and creates another at [decryptedZip]. */
fun interface FileDecrypter {
  suspend operator fun invoke(id: BudgetId, meta: Meta, filePath: Path): DecryptResult
}

/** Decrypts a [Buffer] of bytes and returns another containing the decrypted data */
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
