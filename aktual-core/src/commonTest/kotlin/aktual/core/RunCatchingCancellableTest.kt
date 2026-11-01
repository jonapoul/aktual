package aktual.core

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test

class RunCatchingCancellableTest {
  @Test
  fun `Returns block result`() {
    assertThat(runCatchingCancellable(attempt = { 1 }, onFailure = { 2 })).isEqualTo(1)
  }

  @Test
  fun `Returns failure result`() {
    val e = IllegalStateException("x")
    var caught: Exception? = null
    val result =
      runCatchingCancellable(
        attempt = { throw e },
        onFailure = { ex ->
          caught = ex
          2
        },
      )
    assertThat(result).isEqualTo(2)
    assertThat(caught).isEqualTo(e)
  }

  @Test
  fun `Rethrows cancellation`() {
    assertFailure {
      runCatchingCancellable(attempt = { throw CancellationException("x") }, onFailure = {})
    }
      .isInstanceOf<CancellationException>()
  }

  @Test
  fun `Runs finally on success`() {
    var ran = false
    runCatchingCancellable(attempt = {}, onFailure = {}, finally = { ran = true })
    assertThat(ran).isTrue()
  }

  @Test
  fun `Runs finally on failure`() {
    var ran = false
    runCatchingCancellable(attempt = { error("x") }, onFailure = {}, finally = { ran = true })
    assertThat(ran).isTrue()
  }

  @Test
  fun `Runs finally on cancellation`() {
    var ran = false
    assertFailure {
      runCatchingCancellable(
        attempt = { throw CancellationException("x") },
        onFailure = {},
        finally = { ran = true },
      )
    }
    assertThat(ran).isTrue()
  }
}
