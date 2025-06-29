package actual.account.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordTest {
  private val password = Password("P@ssw0rd!")

  @Test
  fun `Value redacted from toString`() {
    assertEquals(expected = "Password(██)", actual = password.toString())
  }
}
