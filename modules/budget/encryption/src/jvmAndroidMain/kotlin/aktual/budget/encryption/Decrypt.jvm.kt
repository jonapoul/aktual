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
package aktual.budget.encryption

import okio.Buffer
import okio.CipherSource
import okio.Sink
import okio.Source
import okio.buffer
import okio.use
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Throws(UnknownAlgorithmException::class)
actual fun Source.decryptToSink(
  key: ByteArray,
  iv: ByteArray,
  authTag: ByteArray,
  algorithm: String,
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
  val authTagBuffer = Buffer().also { it.write(authTag) }
  val taggedSource = this + authTagBuffer

  CipherSource(taggedSource.buffer(), cipher).buffer().use { source ->
    sink.buffer().use { sink ->
      sink.writeAll(source)
    }
  }
}

internal const val EXPECTED_ALGORITHM = "aes-256-gcm"

private const val AES_GCM_CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
private const val AUTH_TAG_LENGTH = 128
private const val CIPHER_ALGORITHM = "AES"
