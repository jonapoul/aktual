package actual.budget.sync.vm

import actual.api.model.sync.EncryptMeta
import actual.budget.encryption.UnknownAlgorithmException
import actual.budget.encryption.decryptToSink
import actual.budget.model.BudgetId
import actual.core.files.BudgetFiles
import actual.core.files.decryptedZip
import actual.core.model.Base64String
import actual.prefs.KeyPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import alakazam.kotlin.logging.Logger
import kotlinx.coroutines.withContext
import okio.Path
import javax.inject.Inject

// Adapted from packages/loot-core/src/server/encryption/encryption-internals.ts
class DatabaseDecrypter @Inject constructor(
  private val contexts: CoroutineContexts,
  private val keys: KeyPreferences,
  private val files: BudgetFiles,
) {
  suspend operator fun invoke(id: BudgetId, filePath: Path, meta: EncryptMeta): DecryptResult {
    Logger.d("Decrypting $id from $filePath with $meta")
    val key = meta.keyId
      ?.let { keys[it.value] }
      ?: return DecryptResult.MissingKey

    return try {
      decrypt(id, key, filePath, meta)
    } catch (e: UnknownAlgorithmException) {
      DecryptResult.UnknownAlgorithm(e.algorithm)
    } catch (e: Exception) {
      DecryptResult.OtherFailure(e.requireMessage())
    }
  }

  private suspend fun decrypt(
    id: BudgetId,
    key: Base64String,
    encryptedPath: Path,
    meta: EncryptMeta,
  ): DecryptResult {
    val decryptedPath = files.decryptedZip(id, mkdirs = true)

    withContext(contexts.io) {
      files.fileSystem.source(encryptedPath).decryptToSink(
        key = key.decode(),
        iv = meta.iv.decode(),
        authTag = meta.authTag.decode(),
        algorithm = meta.algorithm,
        sink = files.fileSystem.sink(decryptedPath),
      )
    }

    return DecryptResult.Decrypted(decryptedPath)
  }
}
