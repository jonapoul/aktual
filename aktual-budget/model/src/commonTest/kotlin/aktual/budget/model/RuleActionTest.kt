@file:Suppress("JUnitMalformedDeclaration")

package aktual.budget.model

import aktual.budget.model.Field.Category
import aktual.budget.model.Field.Description
import aktual.budget.model.RuleAction.LinkSchedule
import aktual.budget.model.RuleAction.Set
import aktual.budget.model.RuleAction.Set.Options
import app.cash.burst.Burst
import app.cash.burst.burstValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.intellij.lang.annotations.Language

@Burst
class RuleActionTest {
  data class TestCase(val expected: List<RuleAction>, @param:Language("JSON") val json: String)

  @Test
  fun `Parse action from JSON`(case: TestCase = burstValues(TEST_CASE_1, TEST_CASE_2)) {
    assertEquals(
      expected = case.expected,
      actual = Json.decodeFromString(ListSerializer(RuleAction.serializer()), case.json),
    )
  }

  companion object {
    val TEST_CASE_1 =
      TestCase(
        expected =
          listOf(
            Set(field = Description, type = "id", value = "0c76632b-d784-47b0-8391-d9c3067ad6fd"),
            Set(
              field = Category,
              type = "id",
              options = Options(splitIndex = 0),
              value = "51ad3781-25af-4b79-b69c-4e6e62fffabb",
            ),
          ),
        json =
          """
          [
              {
                  "field": "description",
                  "op": "set",
                  "type": "id",
                  "value": "0c76632b-d784-47b0-8391-d9c3067ad6fd"
              },
              {
                  "field": "category",
                  "op": "set",
                  "options": {
                      "splitIndex": 0
                  },
                  "type": "id",
                  "value": "51ad3781-25af-4b79-b69c-4e6e62fffabb"
              }
          ]
          """
            .trimIndent(),
      )

    val TEST_CASE_2 =
      TestCase(
        expected = listOf(LinkSchedule(value = ScheduleId("b08a2607-399b-4a6b-9a5c-3b2d083fe07f"))),
        json =
          """
          [
              {
                  "op": "link-schedule",
                  "value": "b08a2607-399b-4a6b-9a5c-3b2d083fe07f"
              }
          ]
          """
            .trimIndent(),
      )
  }
}
