package actual.api.model.account

import actual.api.json.ActualJson
import kotlin.test.Test
import kotlin.test.assertEquals

class NeedsBootstrapResponseTest {
  @Test
  fun `Deserialize ok`() {
    val json = """
      {
        "status": "ok",
        "data": {
          "bootstrapped": true,
          "loginMethod": "password",
          "availableLoginMethods": [
            {
              "method": "password",
              "active": 1,
              "displayName": "Password"
            }
          ],
          "multiuser": false
        }
      }
    """.trimIndent()

    assertEquals(
      actual = ActualJson.decodeFromString<NeedsBootstrapResponse>(json),
      expected = NeedsBootstrapResponse.Ok(
        data = NeedsBootstrapResponse.Data(
          bootstrapped = true,
          loginMethod = LoginMethod.Password,
          availableLoginMethods = listOf(
            NeedsBootstrapResponse.AvailableLoginMethod(
              method = LoginMethod.Password,
              active = 1,
              displayName = "Password",
            )
          )
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
      actual = ActualJson.decodeFromString<NeedsBootstrapResponse>(json),
      expected = NeedsBootstrapResponse.Error(reason = reason),
    )
  }
}
