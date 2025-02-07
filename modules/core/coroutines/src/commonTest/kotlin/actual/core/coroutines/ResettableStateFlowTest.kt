package actual.core.coroutines

import actual.test.assertEmitted
import app.cash.turbine.test
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ResettableStateFlowTest {
  @Test
  fun `Observe then reset`() = runTest {
    val flow = ResettableStateFlow(value = 123)
    flow.test {
      assertEmitted(123)

      flow.update { 456 }
      assertEmitted(456)

      flow.update { 789 }
      assertEmitted(789)

      flow.reset()
      assertEmitted(123)

      expectNoEvents()
      cancelAndIgnoreRemainingEvents()
    }
  }
}
