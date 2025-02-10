package actual.db.model

import actual.budget.model.ConditionOperator
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ConditionTest {
  @Test
  fun `Decode value array`() = runTest {
    val json = """
      [
        {
          "field": "account",
          "op": "oneOf",
          "type": "id",
          "value": [
            "66f53fa0-ee4e-4f4f-8e36-8a1162e81a89",
            "56972d2e-3ecb-4706-9b4b-2fcba3ab07f3"
          ]
        }
      ]
    """.trimIndent()
    assertEquals(
      expected = listOf(
        Condition(
          field = "account",
          operator = ConditionOperator.OneOf,
          type = "id",
          values = listOf(
            "66f53fa0-ee4e-4f4f-8e36-8a1162e81a89",
            "56972d2e-3ecb-4706-9b4b-2fcba3ab07f3",
          ),
        ),
      ),
      actual = Json.decodeFromString(ListSerializer(Condition.serializer()), json),
    )
  }

  @Test
  fun `Decode single value`() = runTest {
    val json = """
      [
        {
          "field": "category",
          "op": "is",
          "type": "id",
          "value": "ab49064e-5edb-4521-9929-1490f2ee35c2"
        }
      ]
    """.trimIndent()
    assertEquals(
      expected = listOf(
        Condition(
          field = "category",
          operator = ConditionOperator.Is,
          type = "id",
          values = listOf("ab49064e-5edb-4521-9929-1490f2ee35c2"),
        ),
      ),
      actual = Json.decodeFromString(ListSerializer(Condition.serializer()), json),
    )
  }
}
