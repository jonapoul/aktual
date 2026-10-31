package aktual.core

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test

class RunCatchingCancellableTest {
  @Test
  fun `Returns success`() {
    assertThat(runCatchingCancellable { 1 }).isEqualTo(Result.success(1))
  }

  @Test
  fun `Catches exceptions`() {
    val e = IllegalStateException("x")
    assertThat(runCatchingCancellable { throw e }).isEqualTo(Result.failure<Int>(e))
  }

  @Test
  fun `Rethrows cancellation`() {
    assertFailure { runCatchingCancellable { throw CancellationException("x") } }
      .isInstanceOf<CancellationException>()
  }
}
