package actual.test

import app.cash.turbine.TurbineTestContext
import kotlin.test.assertEquals

suspend fun <T> TurbineTestContext<T>.assertEmitted(expected: T) {
  assertEquals(expected = expected, actual = awaitItem())
}

suspend fun <T : Collection<*>> TurbineTestContext<T>.assertEmissionSize(expected: Int) {
  val collection = awaitItem()
  assertEquals(
    expected = expected,
    actual = collection.size,
    message = "Expected collection of size $expected, got $collection",
  )
}

suspend fun <T> TurbineTestContext<List<T>>.assertListEmitted(vararg expected: T) {
  val expectedList = expected.toList()

  val actualList = try {
    awaitItem()
  } catch (t: Throwable) {
    throw AssertionError("Timed out waiting for $expectedList", t)
  }

  assertEquals(
    actual = actualList,
    expected = expected.toList(),
    message = "Expected list $expectedList, got $actualList",
  )
}
