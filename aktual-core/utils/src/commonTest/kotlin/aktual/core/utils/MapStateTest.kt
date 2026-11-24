package aktual.core.utils

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest

class MapStateTest {
  @Test
  fun `Value tracks the source without collecting`() {
    val source = MutableStateFlow(1)
    val mapped = source.mapState { it * 10 }
    assertThat(mapped.value).isEqualTo(10)

    source.update { 2 }
    assertThat(mapped.value).isEqualTo(20)
  }

  @Test
  fun `Emits only when the mapped value changes`() = runTest {
    val source = MutableStateFlow(Pair("a", 1))
    source.mapState { it.first }.test {
      assertThat(awaitItem()).isEqualTo("a")

      source.update { it.copy(second = 2) }
      expectNoEvents()

      source.update { it.copy(first = "b") }
      assertThat(awaitItem()).isEqualTo("b")
    }
  }
}
