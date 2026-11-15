package aktual.core.ui

import assertk.assertThat
import assertk.assertions.containsExactly
import kotlin.test.Test

class StableKeysTest {
  private val keys = StableKeys<String>()

  @Test
  fun `Removing an item keeps the others' keys`() {
    keys.update(listOf("a", "b", "c"))
    assertThat(keys.update(listOf("a", "c"))).containsExactly(0, 2)
  }

  @Test
  fun `Adding an item gives it a new key`() {
    keys.update(listOf("a", "b"))
    assertThat(keys.update(listOf("x", "a", "b"))).containsExactly(2, 0, 1)
  }

  @Test
  fun `Editing an item in place keeps its key`() {
    keys.update(listOf("a", "b", "c"))
    assertThat(keys.update(listOf("a", "B", "c"))).containsExactly(0, 1, 2)
  }

  @Test
  fun `Equal items get distinct keys`() {
    assertThat(keys.update(listOf("a", "a"))).containsExactly(0, 1)
    assertThat(keys.update(listOf("a"))).containsExactly(0)
  }
}
