package aktual.budget.encryption

import aktual.core.model.Key
import aktual.core.model.Password
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.text.Charsets.UTF_8
import okio.ByteString
import okio.ByteString.Companion.toByteString

// Ref packages/loot-core/src/server/encryption/index.ts => createFromPassword()
fun generateKeyFromPassword(password: Password, salt: ByteString): Key {
  val keySpec =
      PBEKeySpec(
          password.value.toCharArray(),
          salt.base64().toByteArray(UTF_8),
          ITERATION_COUNT,
          KEY_LENGTH,
      )
  val factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM)
  val secretKeyBytes = factory.generateSecret(keySpec).encoded
  val aesKey = SecretKeySpec(secretKeyBytes, KEY_ALGORITHM)
  return Key(raw = aesKey, encoded = aesKey.encoded.toByteString())
}

// Ref packages/loot-core/src/server/encryption/index.ts => createFromBase64()
fun generateKeyFromBytes(data: ByteString): Key {
  val secretKey = SecretKeySpec(data.toByteArray(), KEY_ALGORITHM)
  return Key(raw = secretKey, encoded = data)
}

private const val KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA512"
private const val KEY_ALGORITHM = "AES"
private const val ITERATION_COUNT = 10000
private const val KEY_LENGTH = 256
