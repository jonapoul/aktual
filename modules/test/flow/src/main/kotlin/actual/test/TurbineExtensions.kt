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
