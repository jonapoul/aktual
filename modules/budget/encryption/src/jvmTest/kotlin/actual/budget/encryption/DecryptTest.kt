package actual.budget.encryption

import actual.test.TemporaryFolder
import actual.test.assertFailsWith
import actual.test.readBytes
import actual.test.resource
import app.cash.burst.InterceptTest
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.io.encoding.Base64
import kotlin.test.Test

class DecryptTest {
  @InterceptTest val temporaryFolder = TemporaryFolder()

  @Test
  fun decryptSuccessfully() {
    val source = resource("encrypted.zip")
    val destination = temporaryFolder.resolve("decrypted.zip")

    source.decryptToSink(
      key = KEY,
      iv = IV,
      authTag = AUTH_TAG,
      algorithm = EXPECTED_ALGORITHM,
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
      source.decryptToSink(
        key = KEY,
        iv = IV,
        authTag = AUTH_TAG,
        algorithm = "something-unknown",
        sink = temporaryFolder.sink(destination),
      )
    }
  }

  private companion object {
    val KEY = Base64.decode("i1r2wyw7QrknexacWeQlXtPy/NMbS0CsgXNVD0epFAk=")
    val IV = Base64.decode("VoXYpiD8z41ORncY")
    val AUTH_TAG = Base64.decode("CULKUgCtF6/W2m/hwvyh0g==")
  }
}
