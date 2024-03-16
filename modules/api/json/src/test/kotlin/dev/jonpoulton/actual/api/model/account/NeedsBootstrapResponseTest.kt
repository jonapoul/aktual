package dev.jonpoulton.actual.api.model.account

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class NeedsBootstrapResponseTest {
  @Test
  fun `Deserialize ok`() {
    val json = """
      {
          "status": "ok",
          "data": {
              "bootstrapped": true
          }
      }
    """.trimIndent()

    assertEquals(
      actual = Json.decodeFromString<NeedsBootstrapResponse>(json),
      expected = NeedsBootstrapResponse.Ok(
        data = Data(
          bootstrapped = true,
        ),
      ),
    )
  }

  @Test
  fun `Deserialize error`() {
    val reason = "the rain in spain falls mainly on the plain"
    val json = """
      {
          "status": "error",
          "reason": "$reason"
      }
    """.trimIndent()

    assertEquals(
      actual = Json.decodeFromString<NeedsBootstrapResponse>(json),
      expected = NeedsBootstrapResponse.Error(reason = reason),
    )
  }
}
