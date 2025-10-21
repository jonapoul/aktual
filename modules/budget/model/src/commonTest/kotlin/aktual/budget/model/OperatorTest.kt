/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import aktual.test.PrettyJson
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.Serializable
import kotlin.test.Test

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

    assertThat(deserialized).isEqualTo(data)
    assertThat(serialized).isEqualTo(json)
  }
}
