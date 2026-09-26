package aktual.budget.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class CleanupTemplateTest {
  @Test
  fun `Known role picks the matching subtype`() {
    val template =
      Json.decodeFromString<CleanupTemplate>("""{"role":"sink","groupId":null,"weight":2}""")

    assertThat(template).isEqualTo(CleanupTemplate.Sink(groupId = null, weight = 2))
  }

  @Test
  fun `Unrecognised role throws a serialization exception`() {
    assertFailure { Json.decodeFromString<CleanupTemplate>("""{"role":"new-role"}""") }
      .isInstanceOf<SerializationException>()
  }
}
