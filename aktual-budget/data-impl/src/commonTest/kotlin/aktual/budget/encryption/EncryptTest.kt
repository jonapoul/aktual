package aktual.budget.encryption

import aktual.core.model.KeyId
import aktual.core.model.base64
import aktual.test.RESOURCES_DIR
import aktual.test.TemporaryFolder
import aktual.test.readBytes
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import okio.Buffer
import okio.source
import kotlin.random.Random
import kotlin.test.Test

class EncryptTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  @Test
  fun encryptAndDecryptRoundTrip() {
    val encryptedZip = RESOURCES_DIR.resolve("encrypted.zip")
    val encrypted = temporaryFolder / "encrypted.zip"

    // Encrypt the plaintext
    val meta = encryptToSink(
      key = KEY,
      keyId = KeyId("test-key"),
      source = encryptedZip.source(),
      sink = temporaryFolder.sink(encrypted),
      random = Random.Default,
    )

    // Decrypt the encrypted data
    val decrypted = temporaryFolder / "decrypted.zip"
    decryptToSink(
      key = KEY.toByteArray(),
      iv = meta.iv.toByteArray(),
      authTag = meta.authTag.toByteArray(),
      algorithm = meta.algorithm,
      source = temporaryFolder.source(encrypted),
      sink = temporaryFolder.sink(decrypted),
    )

    // Verify the decrypted data matches the original plaintext
    assertThat(temporaryFolder.source(decrypted).readBytes())
      .isEqualTo(encryptedZip.source().readBytes())
  }

  @Test
  fun encryptBufferAndDecryptRoundTrip() {
    val plaintext = "Hello, World! This is a test message for encryption."
    val plaintextBuffer = Buffer().writeUtf8(plaintext)

    // Encrypt the buffer
    val encryptedBuffer = Buffer()
    val meta = encryptToSink(
      key = KEY,
      keyId = KeyId("test-key"),
      source = plaintextBuffer,
      sink = encryptedBuffer,
      random = Random.Default,
    )

    // Decrypt the encrypted buffer
    val decryptedBuffer = Buffer()
    decryptToSink(
      key = KEY.toByteArray(),
      iv = meta.iv.toByteArray(),
      authTag = meta.authTag.toByteArray(),
      algorithm = meta.algorithm,
      source = encryptedBuffer,
      sink = decryptedBuffer,
    )

    // Verify the decrypted data matches the original plaintext
    assertThat(decryptedBuffer.readUtf8())
      .isEqualTo(plaintext)
  }

  private companion object {
    val KEY = "i1r2wyw7QrknexacWeQlXtPy/NMbS0CsgXNVD0epFAk=".base64()
  }
}
