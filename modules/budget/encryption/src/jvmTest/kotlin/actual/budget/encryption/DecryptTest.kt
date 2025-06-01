package actual.budget.encryption

import actual.test.readBytes
import actual.test.resource
import okio.sink
import okio.source
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

class DecryptTest {
  @get:Rule val temporaryFolder = TemporaryFolder()

  @Test
  fun decryptSuccessfully() {
    val source = resource("encrypted.zip")
    val destination = temporaryFolder.root.resolve("decrypted.zip")

    source.decryptToSink(KEY, IV, AUTH_TAG, EXPECTED_ALGORITHM, destination.sink())

    assertContentEquals(
      actual = destination.source().readBytes(),
      expected = resource("expected.zip").readBytes(),
    )
  }

  @Test
  fun invalidAlgorithm() {
    val source = resource("encrypted.zip")
    val destination = temporaryFolder.root.resolve("decrypted.zip")

    assertFailsWith<UnknownAlgorithmException> {
      source.decryptToSink(KEY, IV, AUTH_TAG, algorithm = "something-unknown", destination.sink())
    }
  }

  private companion object {
    val KEY = Base64.decode("i1r2wyw7QrknexacWeQlXtPy/NMbS0CsgXNVD0epFAk=")
    val IV = Base64.decode("VoXYpiD8z41ORncY")
    val AUTH_TAG = Base64.decode("CULKUgCtF6/W2m/hwvyh0g==")
  }
}
