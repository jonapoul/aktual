package aktual.budget.encryption

import aktual.core.model.base64
import aktual.test.TemporaryFolder
import aktual.test.assertFailsWith
import aktual.test.readBytes
import aktual.test.resource
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class DecryptTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  @Test
  fun decryptSuccessfully() {
    val source = resource("encrypted.zip")
    val destination = temporaryFolder / "decrypted.zip"

    decryptToSink(
      key = KEY.toByteArray(),
      iv = IV.toByteArray(),
      authTag = AUTH_TAG.toByteArray(),
      algorithm = EXPECTED_ALGORITHM,
      source = source,
      sink = temporaryFolder.sink(destination),
    )

    assertThat(temporaryFolder.source(destination).readBytes())
      .isEqualTo(resource("expected.zip").readBytes())
  }

  @Test
  fun invalidAlgorithm() {
    val source = resource("encrypted.zip")
    val destination = temporaryFolder.root.resolve("decrypted.zip")

    assertFailsWith<UnknownAlgorithmException> {
      decryptToSink(
        key = KEY.toByteArray(),
        iv = IV.toByteArray(),
        authTag = AUTH_TAG.toByteArray(),
        algorithm = "something-unknown",
        source = source,
        sink = temporaryFolder.sink(destination),
      )
    }
  }

  private companion object {
    val KEY = "i1r2wyw7QrknexacWeQlXtPy/NMbS0CsgXNVD0epFAk=".base64()
    val IV = "VoXYpiD8z41ORncY".base64()
    val AUTH_TAG = "CULKUgCtF6/W2m/hwvyh0g==".base64()
  }
}
