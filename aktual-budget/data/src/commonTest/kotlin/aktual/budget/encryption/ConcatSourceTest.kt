package aktual.budget.encryption

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.buffer

class ConcatSourceTest {
  @Test
  fun a() {
    val a = buffer("abc123")
    val b = buffer("xyz789")
    val c = a + b

    assertThat(c.buffer().use { it.readUtf8() }).isEqualTo("abc123xyz789")
  }

  private fun buffer(data: String): Buffer = Buffer().also { it.write(data.encodeUtf8()) }
}
