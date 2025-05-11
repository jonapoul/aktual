package actual.budget.sync.vm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BytesTest {
  @Test
  fun comparisons() {
    assertTrue(1.bytes < 2.bytes)
    assertTrue(1.bytes <= 2.bytes)
    assertTrue(2.bytes <= 2.bytes)
    assertTrue(2.bytes >= 2.bytes)
    assertEquals(expected = 1.MB, actual = 1000.kB)
  }

  @Test
  fun convertToString() {
    assertEquals(expected = "0 B", actual = 0.bytes.toString())
    assertEquals(expected = "1.2 kB", actual = 1234.bytes.toString())
    assertEquals(expected = "1.23 kB", actual = 1234.bytes.toString(precision = 2))
    assertEquals(expected = "1.234 kB", actual = 1234.bytes.toString(precision = 3))
    assertEquals(expected = "1 kB", actual = 1234.bytes.toString(precision = 0))

    assertEquals(expected = "7.890 kB", actual = 7.89.kB.toString(precision = 3))
    assertEquals(expected = "7.890 MB", actual = 7.89.MB.toString(precision = 3))
    assertEquals(expected = "7.890 GB", actual = 7.89.GB.toString(precision = 3))
    assertEquals(expected = "7.890 TB", actual = 7.89.TB.toString(precision = 3))
  }

  @Test
  fun multiply() {
    assertEquals(expected = 16.bytes, actual = 8.bytes * 2)
    assertEquals(expected = 8.bytes, actual = 8.bytes * 1)
  }
}
