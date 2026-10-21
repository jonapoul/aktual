package aktual.budget.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlin.test.Test

class ScheduleAmountTest {
  @Test
  fun `Amounts between two values use the rounded average`() {
    assertThat(parseScheduleAmount("-1500")).isEqualTo(-1500L)
    assertThat(parseScheduleAmount("""{"num1":-1000,"num2":-2001}""")).isEqualTo(-1500L)
    assertThat(parseScheduleAmount(null)).isNull()
  }
}
