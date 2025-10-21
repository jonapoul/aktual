/**
 * Copyright 2025 Jon Poulton
 * SPDX-License-Identifier: Apache-2.0
 */
package aktual.budget.model

import aktual.test.PrettyJson
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test

class ReportConditionTest {
  @Test
  fun `Multiple conditions`() {
    val json = """
      [
        {
          "field": "account",
          "op": "is",
          "value": "eb08ea4f-bbb0-437f-873a-1fdee4154683",
          "type": "id"
        },
        {
          "field": "payee",
          "op": "is",
          "value": "92ac5221-2605-419d-821a-6ec04ea38b57",
          "type": "id"
        }
      ]
    """.trimIndent()

    val data = listOf(
      ReportCondition(
        field = ReportCondition.Field.Account,
        operator = Operator.Is,
        type = ReportCondition.Type.Id,
        value = JsonPrimitive("eb08ea4f-bbb0-437f-873a-1fdee4154683"),
      ),
      ReportCondition(
        field = ReportCondition.Field.Payee,
        operator = Operator.Is,
        type = ReportCondition.Type.Id,
        value = JsonPrimitive("92ac5221-2605-419d-821a-6ec04ea38b57"),
      ),
    )

    val serialized = PrettyJson.encodeToString(data)
    assertThat(serialized).isEqualTo(json)

    val deserialized = PrettyJson.decodeFromString(ReportCondition.ListSerializer, serialized)
    assertThat(deserialized).isEqualTo(data)
  }
  @Test
  fun `Single condition with array value`() {
    val json = """
      [
        {
          "field": "category",
          "op": "oneOf",
          "value": [
            "e9e1e6e6-6c1b-44c3-9392-8e435ae662f3",
            "711b4c36-86f5-4206-a815-01bfa83cc5b0",
            "69779c92-2c05-44d3-8b7d-1f4be08ac423",
            "8cf36dde-948f-49d5-9648-754df0000b72",
            "7e740971-7cfe-46bf-8ea4-0d428a65f7a3",
            "8114e829-af7c-4488-ad74-783bd91a9718",
            "9dfb243f-9ab8-4b7d-9411-a79f78f23125",
            "474c707c-fab7-490d-9b33-ea8912c6fe9b"
          ],
          "type": "id"
        }
      ]
    """.trimIndent()

    val data = listOf(
      ReportCondition(
        field = ReportCondition.Field.Category,
        operator = Operator.OneOf,
        type = ReportCondition.Type.Id,
        value = JsonArray(
          content = listOf(
            "e9e1e6e6-6c1b-44c3-9392-8e435ae662f3",
            "711b4c36-86f5-4206-a815-01bfa83cc5b0",
            "69779c92-2c05-44d3-8b7d-1f4be08ac423",
            "8cf36dde-948f-49d5-9648-754df0000b72",
            "7e740971-7cfe-46bf-8ea4-0d428a65f7a3",
            "8114e829-af7c-4488-ad74-783bd91a9718",
            "9dfb243f-9ab8-4b7d-9411-a79f78f23125",
            "474c707c-fab7-490d-9b33-ea8912c6fe9b",
          ).map(::JsonPrimitive),
        ),
      ),
    )

    val serialized = PrettyJson.encodeToString(data)
    assertThat(serialized).isEqualTo(json)

    val deserialized = PrettyJson.decodeFromString(ReportCondition.ListSerializer, serialized)
    assertThat(deserialized).isEqualTo(data)
  }
}
