package aktual.core.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.json.Json

class PossibleRoleTest {
  @Test
  fun `Decode known values`() {
    assertThat(decode("ADMIN")).isEqualTo(Admin)
    assertThat(decode("BASIC")).isEqualTo(Basic)
  }

  @Test
  fun `Decode unknown value as fallback`() {
    assertThat(decode("OWNER")).isEqualTo(Unknown)
  }

  @Test
  fun `Encode by serial name`() {
    assertThat(Json.encodeToString<PossibleRole>(Admin)).isEqualTo("\"ADMIN\"")
  }

  private fun decode(value: String) = Json.decodeFromString<PossibleRole>("\"$value\"")
}
