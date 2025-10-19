/**
 * Copyright 2025 Jon Poulton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
