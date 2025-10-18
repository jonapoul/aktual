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
package actual.budget.encryption

import actual.core.model.Base64String
import actual.core.model.Key
import actual.core.model.Password
import actual.core.model.base64
import dev.zacsweers.metro.Inject
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8

// Ref packages/loot-core/src/server/encryption/index.ts
@Inject
class KeyGenerator {
  // See createFromPassword()
  operator fun invoke(password: Password, salt: Base64String): Key {
    val keySpec = PBEKeySpec(
      password.value.toCharArray(),
      salt.value.toByteArray(UTF_8),
      ITERATION_COUNT,
      KEY_LENGTH,
    )
    val factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
    val secretKeyBytes = factory.generateSecret(keySpec).encoded
    val aesKey = SecretKeySpec(secretKeyBytes, KEY_ALGORITHM)
    return Key(
      raw = aesKey,
      base64 = aesKey.encoded.base64,
    )
  }

  // See createFromBase64()
  operator fun invoke(base64String: Base64String): Key {
    val decodedBytes = base64String.decode()
    val secretKey = SecretKeySpec(decodedBytes, KEY_ALGORITHM)
    return Key(raw = secretKey, base64 = base64String)
  }

  private companion object {
    const val KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA512"
    const val KEY_ALGORITHM = "AES"
    const val ITERATION_COUNT = 10000
    const val KEY_LENGTH = 256
  }
}
