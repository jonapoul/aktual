package aktual.core.model

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class TokenTest {
  @Test
  fun `API keys`() {
    assertThat(Token("actual_q7Xc2mVb9LpR4tYw8NzK3hJf6Gd1Sa5Ue0Io").isApiKey).isTrue()
  }

  @Test
  fun `Session tokens`() {
    assertThat(Token("8b1f0c3e-5a7d-4f2b-9c61-2e4d8a9f7b10").isApiKey).isFalse()
  }
}
