package actual.budget.model

import actual.test.PrettyJson
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals

class OperatorTest {
  @Serializable
  data class TestData(val operators: List<Operator>)

  @Test
  fun serialize() {
    val data = TestData(operators = Operator.entries)
    val json = """
      {
        "operators": [
          "and",
          "contains",
          "doesNotContain",
          "gt",
          "gte",
          "hasTags",
          "is",
          "isapprox",
          "isbetween",
          "isNot",
          "lt",
          "lte",
          "matches",
          "notOneOf",
          "offBudget",
          "onBudget",
          "oneOf"
        ]
      }
    """.trimIndent()

    val serialized = PrettyJson.encodeToString(data)
    val deserialized = PrettyJson.decodeFromString<TestData>(json)

    assertEquals(expected = data, actual = deserialized)
    assertEquals(expected = json, actual = serialized)
  }
}
