package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isLessThan
import assertk.assertions.isTrue
import assertk.assertions.isZero
import kotlin.test.Test

class IdsTest {
  @Test
  fun `toString returns the raw value`() {
    assertThat(AccountId("a").toString()).isEqualTo("a")
  }

  @Test
  fun `compareTo delegates to the raw value`() {
    assertThat(AccountId("a").compareTo(AccountId("b"))).isLessThan(0)
    assertThat(AccountId("b").compareTo(AccountId("a"))).isGreaterThan(0)
    assertThat(AccountId("a").compareTo(AccountId("a"))).isZero()
  }

  @Test
  fun `comparison operators work via the generated Comparable supertype`() {
    assertThat(AccountId("a") < AccountId("b")).isTrue()
  }
}
