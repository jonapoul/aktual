/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.encryption

import aktual.core.model.Base64String
import aktual.core.model.Key
import aktual.core.model.Password
import aktual.core.model.base64
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
