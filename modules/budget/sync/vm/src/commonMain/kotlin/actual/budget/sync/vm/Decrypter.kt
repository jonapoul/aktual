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
package actual.budget.sync.vm

import actual.api.model.sync.EncryptMeta
import actual.budget.encryption.UnknownAlgorithmException
import actual.budget.encryption.decryptToSink
import actual.budget.model.BudgetFiles
import actual.budget.model.BudgetId
import actual.budget.model.decryptedZip
import actual.prefs.KeyPreferences
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.Inject
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
  ): DecryptResult {
    val key = keys[meta.keyId] ?: return DecryptResult.MissingKey

    return try {
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
    } catch (e: Exception) {
      DecryptResult.OtherFailure(e.requireMessage())
    }
  }
}
