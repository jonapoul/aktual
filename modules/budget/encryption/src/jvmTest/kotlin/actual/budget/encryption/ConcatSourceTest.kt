package actual.budget.encryption

import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertEquals

class ConcatSourceTest {
  @Test
  fun a() {
    val a = buffer("abc123")
    val b = buffer("xyz789")
    val c = a + b

    assertEquals(
      expected = "abc123xyz789",
      actual = c.buffer().use { it.readUtf8() },
    )
  }

  private fun buffer(data: String): Buffer = Buffer().also { it.write(data.encodeUtf8()) }
}
